/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */
 
/*****************************************************************************
 * Description: wrapper protocol implementation for Win32 systems            *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.1 $                                            *
 *****************************************************************************/
#include "jserv_wrapper.h"

#ifdef WIN32
/*****************************************************************************
 * Win32 specific variables and procedures                                   *
 *****************************************************************************/
static HANDLE wrapper_thread=NULL;
static HANDLE wrapper_semaphore=NULL;
static HANDLE wrapper_event=NULL;
static HANDLE wrapper_process=NULL;

static int wrapper_controller (wrapper_config *cfg);
static HANDLE wrapper_execute (wrapper_config *cfg);

/*****************************************************************************
 * Win32 code                                                                *
 *****************************************************************************/

/* ========================================================================= */
/* Our controller thread */
static int wrapper_controller (wrapper_config *cfg) {
    int ret=WAIT_TIMEOUT;
    HANDLE towait[]={
        wrapper_event,
        wrapper_process,
    };

    /* Wait and loop until we get a wrapper event signalled */
    while ((ret=WaitForMultipleObjects(2, towait, FALSE, INFINITE))
           !=WAIT_OBJECT_0) {
        /* We got the event so we shut down our thread */
        if (ret==(WAIT_OBJECT_0+1)) {
            /* Log the JVM crash */
            jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                        "Java Virtual Machine crashed");
            /* avoid endless spawn/die cycles */
            if (wrapper_check_restart_time_ok()) {
                /* Try to rebuild the JVM */
                wrapper_process=wrapper_execute(cfg);
                /* Check if start was successful, exit thread if it was not */
                if (wrapper_process==NULL) {
                    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                                "can not restart Java Virtual Machine");
                    ExitThread(1);
                /* Place process on its place and return waiting */
                } else towait[1]=wrapper_process;
            } else { /* too many restart attempts */
                jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                            "too many restart attempts w/in a short timeframe; no more retries.\n" \
                            "There may be an error in your Apache JServ configuration.\n" \
                            "To debug, please enable all log options in jserv.properties:\n" \
                            "log=true\n" \
                            "log.file=/usr/local/apache/var/log/jserv.trace\n" \
                            "log.timestamp=true\n" \
                            "log.dateFormat=[yyyyMMdd HHmmss:SSS]\n" \
                            "log.channel.init=true\n" \
                            "log.channel.terminate=true\n" \
                            "log.channel.serviceRequest=true\n" \
                            "log.channel.authentication=true\n" \
                            "log.channel.requestData=true\n" \
                            "log.channel.responseHeaders=true\n" \
                            "log.channel.signal=true\n" \
                            "log.channel.exceptionTracing=true\n" \
                            "log.channel.servletManager=true\n" \
                            "restart apache, access your servlet, and examine your:\n" \
                            "- Apache error log as specified in the ErrorLog directive;\n" \
                            "- your jserv log file as specified in the ApJServLogFile directive;\n" \
                            "- and the jserv log file as specified in jserv.properties for possible clues.\n" );
                ExitThread(1);
            }
        /* What the HACK did we got??? Ignore it but place a log */
        } else {
            jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                        "unknown controller interruption");
        }
    }
    /* We got a STOP event... */
    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s (PH=%d)",
                "controller got stop event",wrapper_process);

    /* Check wether JVM already died... */
    ret=WaitForSingleObject(wrapper_process,0);
    if (ret==WAIT_TIMEOUT) {
        /* Signalling JServ to shut down */
        jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                    "Sending signal to JServ");
        jserv_protocol_function(cfg->config->protocol,cfg->config,
                                JSERV_SHUTDOWN,NULL);

        /* Wait 4 seconds for JVM shutdown */
        ret=WaitForSingleObject(wrapper_process,4000);
        /* If JVM did not shut down TERMINATE IT */
        if (ret==WAIT_TIMEOUT) {
            TerminateProcess(wrapper_process,0);
            jserv_error(JSERV_LOG_ERROR,cfg->config,"wrapper: %s",
                        "Java Virtual Machine did not exit, terminated");
        }
    }

    /* Close handle and return */
    CloseHandle(wrapper_process);
    wrapper_process=NULL;
    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                "Java Virtual Machine stopped");
    return 0;
}

/* ========================================================================= */
/* Create our process */
static HANDLE wrapper_execute (wrapper_config *cfg) {
    STARTUPINFO startup_info; 
    PROCESS_INFORMATION process_info;
    int ret;
    int processflags=CREATE_NEW_PROCESS_GROUP | DETACHED_PROCESS;
    char *commandline=NULL;
    char *environment=NULL;
    char *binparam=NULL;
    int char_block_size = 8196;
    int string_size = 0;
    int temp_int = 0;
    char *temp_char_ptr;
    wrapper_property *cur=cfg->environment;
    wrapper_property_list_node *params_list = cfg->binparam;

    /* Setup binparameters */
    binparam = ap_palloc(wrapper_pool, char_block_size * sizeof(char));
    *binparam = '\0';
    while (params_list != NULL && params_list->name != NULL) {
        temp_int = strlen(params_list->name);
        temp_char_ptr = binparam;
        while (string_size + temp_int + 2 >= char_block_size) { /* need more */
            char_block_size = char_block_size * 2;              /* memory. */
            binparam = ap_palloc(wrapper_pool, char_block_size * sizeof(char));
        }
        if (temp_char_ptr != binparam) {
            strncpy(binparam, temp_char_ptr, string_size);
        }
        strncpy (binparam + string_size, params_list->name, temp_int);
        params_list = params_list->next;
        string_size += temp_int;
        *(binparam + string_size++) = ' ';
    }
    *(binparam+string_size) = '\0';

    /* Setup command line */
    commandline=ap_pstrcat(wrapper_pool, cfg->bin, " ", binparam, " ", 
                           cfg->class, " \"", cfg->config->properties, "\" ",
                           cfg->classparam, NULL);
                           
    /* Setup environment */
    char_block_size = 8196;
    string_size = 0;
    environment = ap_palloc (wrapper_pool, char_block_size * sizeof(char));
    while (cur!=NULL && cur->name != NULL) {
        temp_int = strlen(cur->name) + 2;
        if (cur->value != NULL) temp_int += strlen(cur->value);

        temp_char_ptr = environment;
        while (string_size + temp_int + 1 >= char_block_size) { /* need more */
            char_block_size = char_block_size * 2;              /* memory. */
            environment = ap_palloc(wrapper_pool, char_block_size * sizeof(char));
        }
        if (temp_char_ptr != environment) {                 /* we allocated */
            strncpy(environment, temp_char_ptr, string_size); /* more memory*/
        }

        /* copy stuff over, one element at a time. snprintf would be briefer.*/
        strcpy (environment+string_size, cur->name);
        string_size += strlen(cur->name);
        *(environment + string_size++) = '=';
        if (cur->value != NULL) {
            strcpy (environment + string_size, cur->value);
            string_size += strlen(cur->value) + 1; /* strcpy null terminates */
        } else  *(environment + string_size++) = '\0';     /* null terminate */

        cur=cur->next;
    }
    /* strcpy null terminates environment. but it doesn't hurt to make sure. */
    *(environment + string_size) = '\0';

    /* Clear our STARTUPINFO structure */
    startup_info.cb=sizeof(STARTUPINFO);
    startup_info.lpReserved=NULL;
    startup_info.lpDesktop=NULL;
    startup_info.lpTitle=NULL;
    startup_info.dwX=0;
    startup_info.dwY=0;
    startup_info.dwXSize=0;
    startup_info.dwYSize=0;
    startup_info.dwXCountChars=0;
    startup_info.dwYCountChars=0;
    startup_info.dwFillAttribute=0;
    startup_info.dwFlags=STARTF_USESTDHANDLES;
    startup_info.wShowWindow=0;
    startup_info.cbReserved2=0;
    startup_info.lpReserved2=NULL;
    startup_info.hStdInput=NULL;
    startup_info.hStdOutput=GetStdHandle(STD_OUTPUT_HANDLE);
    startup_info.hStdError=GetStdHandle(STD_ERROR_HANDLE);

    /* Clear our PROCESS_INFORMATION structure */ 
    process_info.hProcess=NULL;
    process_info.hThread=NULL;
    process_info.dwProcessId=0;
    process_info.dwThreadId=0;

    /* Create the process */
    ret=CreateProcess(NULL, 
        commandline,    /* the command line to start */
        NULL,           /* process security attributes */
        NULL,           /* primary thread security attributes */
        TRUE,           /* handles are not inherited */
        processflags,   /* we specify new process group */
        environment,    /* use parent's environment */
        NULL,           /* use parent's current directory */
        &startup_info,  /* STARTUPINFO pointer */
        &process_info); /* PROCESS_INFORMATION pointer */

    /* Check if virtual machine started */
    if (ret==FALSE) {
        int err=GetLastError();
        /* This was placed to handle the Swedish WinNT bug */
        if (err!=NO_ERROR) {
            jserv_error(JSERV_LOG_INFO,cfg->config,
                        "wrapper: can not execute \"%s\" (ERR=%d)",
                        commandline, err);
            return NULL;
        }
    }

    /* Now check if we have a process handle again for the Swedish WinNT bug */
    if (process_info.hProcess==NULL) {
    jserv_error(JSERV_LOG_INFO,cfg->config,
                "wrapper: can not execute \"%s\"",commandline);
        return NULL;
    }

    jserv_error(JSERV_LOG_INFO,cfg->config,
                "wrapper: Java Virtual Machine started (PID=%d)",
                process_info.dwProcessId);
    return process_info.hProcess;
}

/* ========================================================================= */
/* Create thread, process, semaphore and event */
int wrapper_create (wrapper_config *cfg) {
    int ret;
    int discard;

    /* Create/Open our semaphore. The semaphore is nedeed to prevent multiple
       startup of the Java Virtual Machine */
	wrapper_semaphore=CreateSemaphore(
		NULL,                       /* do not inherit this semaphore handle */
		3,                          /* initial value */
		3,                          /* maximum value */
		"jserv-wrapper-semaphore"); /* our semaphore name */
    if (wrapper_semaphore==NULL) {
    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                "can not open/create semaphore");
        return -1;
	}

    /* The semaphore was initialised with value 3. In first startup (call it
       "defensive programming") it is brought back to 2 and its status is
       signalled. In second call its value is brought from 1 to 0 and status
       is signalled. In further inits its value is 0 and  its status is NON
       signalled (we should not fire JVM) */
    ret=WaitForSingleObject(wrapper_semaphore,0);
    if (ret==WAIT_TIMEOUT) return 0;
    /* In first startup semaphore value is brought from 2 to 1 and status is
       signalled (then we should not fire JVM), in second call its value is
       brought from 1 to 0 and status is NOT signalled (we fire JVM). Further
       calls do not get here (since they return to caller in above line) */
    ret=WaitForSingleObject(wrapper_semaphore,0);
    if (ret==WAIT_OBJECT_0) return 0;

    /* We have a TIMEOUT on second semaphore decrement... We start JVM */
    wrapper_process=wrapper_execute(cfg);
    if (wrapper_process==NULL) {
        jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                    "can not execute Java Virtual Machine");
        wrapper_destroy(cfg);
        return -1;
    }

    /* Create the stop event for controller thread */
	wrapper_event=CreateEvent(
		NULL,                   /* the event is not inheritable */
		TRUE,                   /* we want a manual reset for this event */
		FALSE,                  /* start event unsignalled */
		"jserv-wrapper-event"); /* our event name */
    if (wrapper_event==NULL) {
    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                "can not open/create event");
        wrapper_destroy(cfg);
        return -1;
	}

    /* Create the controller thread. This will handle JVM control */
    wrapper_thread=CreateThread(
        NULL,       /* no security, this thread handle will not be inherited */
        0,          /* stack size (0 means as current thread) */
        (LPTHREAD_START_ROUTINE) wrapper_controller,    /* thread routine */
        cfg,        /* our server configuration is passed to thread */
        0,          /* creation flags (0 means run now) */
        &discard);      /* we are not interested in thread id */
  
    /* Checks wether controller thread is active, if not fails execution */
    if (wrapper_thread==NULL) {
    jserv_error(JSERV_LOG_INFO,cfg->config,
                "wrapper: controller thread can not start (ERR=%d)",
                GetLastError());
        wrapper_destroy(cfg);
        return -1;
    }
    jserv_error(JSERV_LOG_INFO,cfg->config,
                "wrapper: controller started (PID=%d)",getpid());

    /* Log our initialization */
    return 0;
}

/* ========================================================================= */
/* Destroy all */
int wrapper_destroy (wrapper_config *cfg) {
    int ret;

    /* Check our thread status and terminate it */
    if (wrapper_thread!=NULL) {
        if (wrapper_event!=NULL) {
            /* Post the quit message */
            SetEvent(wrapper_event);

            /* Wait 5 second for thread and process to terminate */
            ret=WaitForSingleObject(wrapper_thread,5000);

            /* Wait did not go thru... terminate the thread */
            if (ret!=WAIT_OBJECT_0) {
                TerminateThread(wrapper_thread,0);
                jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                            "thread did not end, terminating it");
            }
        /* The event was not valid, kill the thread */
        } else  {
            TerminateThread(wrapper_thread,0);
            jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                        "termination event was not found, terminating thread");
        }
        /* Close our thread handle */
        CloseHandle(wrapper_thread);
        jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                    "controller thread stopped");
    }

    /* Check our process status and terminate it */
    if (wrapper_process!=NULL) {
        /* Send the function request for restart */
        jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s", 
                    "Java Virtual Machine still alive after controller kill");

        /* Wait max 5 seconds for JVM to close */
        TerminateProcess(wrapper_process,0);
        CloseHandle(wrapper_process);
        wrapper_process=NULL;
        jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s",
                    "Java Virtual Machine stopped");
    }

    /* Semaphore and event handles are closed when the process exits */
    jserv_error(JSERV_LOG_INFO,cfg->config,"wrapper: %s (PID=%d)",
                "Shutdown done",getpid());
    return 0;
}


/*****************************************************************************
 * Standalone wrapper code                                                   *
 *****************************************************************************/

/* ========================================================================= */
/* Wrapper standalone startup code */
void wrapper_standalone_startup(void) {
    WORD ws_version=MAKEWORD(1,1); 
    WSADATA ws_data;

    /* Initialize WINSOCK dlls */
    if (WSAStartup(ws_version, &ws_data)!=0) {
        printf("Cannot initialize Windows sockets DLLs\n");
        exit(1);
    }
}

/* ========================================================================= */
/* Wrapper standalone handler (CTRL-C CTRL-BREAK SHUTDOWN...) */
int wrapper_standalone_handler(int key) {
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
                "Standalone: Shutting down JServ");
    /* Cleanup this thing */
    if (wrapper_destroy(wrapper_data)!=0)
        jserv_error(JSERV_LOG_INFO, wrapper_data->config, 
                    "Standalone: Error shutting down JServ");
    ExitProcess(0);
    return TRUE;
}

/* ========================================================================= */
/* Wrapper standalone process (wait for user interruption) code */
void wrapper_standalone_process(void) {
    int ret;

    /* Set handler for CTRL-C... */
    SetConsoleCtrlHandler((PHANDLER_ROUTINE)wrapper_standalone_handler,TRUE);

    /* Process CTRL-R (restart) */
    while (ret=getchar());

    /* Something strange  */
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
                "Standalone: Shutting down JServ (got strange char)");
    /* Cleanup this thing */
    if (wrapper_destroy(wrapper_data)!=0)
        jserv_error(JSERV_LOG_INFO, wrapper_data->config, 
                    "Standalone: Error shutting down JServ");
    exit (1);
}

#endif /* ifdef WIN32 */
