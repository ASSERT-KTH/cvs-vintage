/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
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
 * Author:      Pierpaolo Fumagalli <ianosh@@iname.com>,                      *
 *              Ed Korthof <ed@@ultimanet.com>                                *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/
#include "jserv.h"
#include "http_conf_globals.h"
#include "jserv_wrapper.h"
#include <signal.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

#ifndef WIN32
/*****************************************************************************
 * Unix specific variables and procedures                                    *
 *****************************************************************************/
typedef void (*sighandler_t)(int);

static pid_t wrapper_pid = 0;
static pid_t jvm_pid = 0;

/*****************************************************************************
 * Unix code                                                                 *
 *****************************************************************************/

/* ========================================================================= */
/* Signal handler for SIGTERM (standalone wrapper), and for Apache 1.2, for
 * when the parent Apache process dies.   JServ should kill the JVM. */
void wrapper_shutdown(int x) {
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
                "Wrapper: Shutting down JServ (PID=%d) (sig %d)",getpid(), x);
    /* Cleanup this thing */
    if (wrapper_shutdown_core(wrapper_data)!=0)
        jserv_error(JSERV_LOG_INFO, wrapper_data->config, 
                    "Wrapper: Error shutting down JServ");
    exit(0);
}

/* ========================================================================= */
/* Signal handler for SIGALRM (used to measure timeouts in pinging JVM)      */
/* this is also used from the loop to kill JVM                               */
void kill_hung_jvm(int signum) {
  int counter = 0;
  
  /* Is jvm is really started? If not - do nothing */
  if( jvm_pid == 0)
    return;

  if( signum == 0) {
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
		"wrapper: Java VM is not responding (PID=%d)", getpid());
  } else {
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
		"wrapper: Java VM not responding (PID=%d) [timeout]", getpid());
  }
  /* IF JVM is not responding to connections, we can't do   *
   * anything but kill it.                                  */
  kill(jvm_pid, SIGTERM);

  /* give the VM as long as five seconds to die gracefully */
  while (counter++ < 5) {
    if (waitpid(jvm_pid, NULL, WNOHANG) > 0)
      break;
    sleep(1);
  }
  if( waitpid(jvm_pid,NULL,WNOHANG)==0 ) {
    jserv_error(JSERV_LOG_INFO, wrapper_data->config,
		"wrapper: kill (SIGKILL) Java VM (PID=%d)", getpid());
    kill(jvm_pid, SIGKILL);
    waitpid(jvm_pid,NULL,0); /* if it does not died, KERNEL bug! */
  }
  jvm_pid=0;
}


/* This function provides some standard debugging data in the event that
 * either the VM cannot be started, or the VM dies repeatedly.
 */
static void dump_debugging_info (char ** arg, char ** env) {
    int x;

    jserv_error(JSERV_LOG_ERROR,wrapper_data->config,
                "wrapper: printing debugging information (command line, env)");

    /* Dump arguments and environment */
    x=0; while (arg[x]!=NULL) {
        jserv_error(JSERV_LOG_ERROR,wrapper_data->config,
                    "wrapper: argument[%2d] %s",x,arg[x]);
		++x;
	}
    x=0; while (env[x]!=NULL) {
        jserv_error(JSERV_LOG_ERROR,wrapper_data->config,
                    "wrapper: environment[%2d] %s",x,env[x]);
		++x;
	}
}

/* This function starts the VM, given the appropriate context.  It doesn't
 * fork. */
static void wrapper_exec_jserv_core(char * arg0, char ** arg, char ** env) {
    /* Execute the JVM */
    jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                "wrapper: Java VM spawned (PID=%d, PPID=%d)",
                getpid(),getppid());
    ap_cleanup_for_exec();
    execve(arg0,arg,env);

    /* We reached this point... We were unable to start */
    jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                "wrapper: Java Virtual Machine unable to start (ERR=%d: %s)",
                errno, strerror(errno));
    dump_debugging_info(arg, env);
    exit(1);
}

/* ========================================================================= */
/* Start the Java Virtual Machine and the watcher process */
pid_t wrapper_spawn(void) {
    wrapper_config *cfg=wrapper_data;
    wrapper_property *cur=cfg->environment;
    wrapper_property_list_node * binparams = cfg->binparam;
    pid_t proc;
    int x;
    char **arg;
    char **env;

   /* Apache on Unix has a 2 phases startup initialization : */
   /* we will here spawn only one JVM in the 2nd pass.       */ 
    if (ap_standalone && getppid() == 1) {
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                    "Apache-JServ 2nd  initialization starting JVM now: %d %d %d", ap_standalone, getpid(), getppid()); 
    }
    else {
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                    "Apache-JServ 1rst  initialization: JVM will be started later %d %d %d", ap_standalone, getpid(), getppid()); 
        return 0;
    }

    /* Fork the process and return child pid to parent */
    proc=fork();
    /*jserv_error(JSERV_LOG_INFO,wrapper_data->config,
     *          "fork: %d; PID: %d; PPID: %d", proc, getpid(), getppid());*/
    
    if (proc < 0) { /* error forking. */
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                    "Error forking for Apache-JServ initialization: %s",
                    strerror(errno));
        exit(1);
    }

    if (proc!=0) return proc;
        
    /* If we get a TERM signal, shut down the JVM nicely, then exit.
     */
    signal(SIGTERM, wrapper_shutdown);

    x = 5;
    while (binparams != NULL) {
        ++x;
        binparams = binparams->next;
    }
    binparams = cfg->binparam;

    arg = (char **)malloc(x*sizeof(char *));
    env = (char **)malloc(100*sizeof(char *));

    /* Setup environment */
    for (x=0;((x<99) && (cur!=NULL)); x++) {
        int len=0;

        len=strlen(cur->name);
        if (cur->value!=NULL) len+=strlen(cur->value);
        env[x]=(char *)malloc(len+2);
        if (cur->value!=NULL) sprintf(env[x],"%s=%s",cur->name,cur->value);
        else  sprintf(env[x],"%s=",cur->name);
        cur=cur->next;
    }
    env[x]=NULL;

    /* Setup arguments */

    x=0; arg[x++]=strdup(cfg->bin);
    while (binparams != NULL) {
        if (binparams->name != NULL && *(binparams->name) != '\0') {
            arg[x++] = strdup(binparams->name);
        }
        binparams = binparams->next;
    }
    if (cfg->class[0]!='\0') arg[x++]=strdup(cfg->class);
    arg[x++]=strdup(cfg->config->properties);
    if (cfg->classparam[0]!='\0') arg[x++]=strdup(cfg->classparam);
    arg[x]=NULL;

    /* Setup our process group. Process group is changed because Apache when
       exiting does a SIGKILL to it's process group. And we don't want to be
       SIGKILLed */
    setpgid(0,0);

    /* Change our UID/GID. */

    /* If we're not root, we don't have to do any of this. */
    if (!geteuid()) {
        /* First, change the group id.  This is code copied from http_main.c;
         * we can't use the function there because it's static to that file. */
	char *name;

	/* Get username if passed as a uid */

	if (ap_user_name[0] == '#') {
	    struct passwd *ent;
	    uid_t uid = atoi(&ap_user_name[1]);

	    if ((ent = getpwuid(uid)) == NULL) {
		ap_log_error(APLOG_MARK, APLOG_ALERT, cfg->config->server,
			 "getpwuid: couldn't determine user name from uid %u, "
			 "you probably need to modify the User directive",
			 (unsigned)uid);
	        exit(1); /* don't know enough to exit cleanly... oh well. */
	    }

	    name = ent->pw_name;
	}
	else
	    name = ap_user_name;

#ifndef OS2
	/* OS/2 dosen't support groups. */

	/* Reset `groups' attributes. */

	if (initgroups(name, ap_group_id) == -1) {
	    ap_log_error(APLOG_MARK, APLOG_ALERT, cfg->config->server,
			"initgroups: unable to set groups for User %s "
			"and Group %u", name, (unsigned)ap_group_id);
	    exit(1); /* don't know enough to exit cleanly... oh well. */
	}
#ifdef MULTIPLE_GROUPS
	if (getgroups(NGROUPS_MAX, group_id_list) == -1) {
	    ap_log_error(APLOG_MARK, APLOG_ALERT, cfg->config->server,
			"getgroups: unable to get group list");
	    exit(1); /* don't know enough to exit cleanly... oh well. */
	}
#endif
	if (setgid(ap_group_id) == -1) {
	    ap_log_error(APLOG_MARK, APLOG_ALERT, cfg->config->server,
			"setgid: unable to set group id to Group %u",
			(unsigned)ap_group_id);
	    exit(1); /* don't know enough to exit cleanly... oh well. */
	}
#endif
        /* Change uid to the server's User, if appropriate */
        if (
#ifdef _OSD_POSIX
            os_init_job_environment(cfg->config->server, ap_user_name, 0) != 0 ||
#endif
            setuid(cfg->config->server->server_uid) == -1) {

            jserv_error(JSERV_LOG_INFO, wrapper_data->config,
                        "wrapper: Unable to change uid to start JVM: exiting");
            exit(1);
        }
    }

    /* Under Apache 1.2 the permanent pool is never cleared when exiting. To
     * catch an exit condition we have to fork once more, and then wait for
     * Apache process disappear. When this disappears we exit.
     *
     * Under Apache 1.3, there's a function to send a SIGTERM to this process
     * -- but we still check every second to make sure the parent process
     * is still alive, because it doesn't hurt (and could help).  This
     * might be needed for restarts (SIGHUP) or graceful restarts (SIGUSR1)...
     *
     * Technically there's no reason we couldn't be able to use SIGCHLD
     * here -- but it doesn't offer any significant advantages, and might
     * create incompatibilities.
     */
        
    if (getppid() == 0) {
        jserv_error(JSERV_LOG_INFO, wrapper_data->config,
            "wrapper: parent httpd died before the VM was spawned.\n"
            "wrapper: something is wrong; not starting JVM for JServ.\n");
        exit(1);
    }

    jvm_pid = fork();

    if (jvm_pid != 0) { /* The parent in this fork will be the watcher process */
        int last_restart = time(NULL);
        jserv_error(JSERV_LOG_DEBUG,wrapper_data->config,
                "wrapper: watching processes in %d seconds(PID=%d,PPID=%d,JVM PID=%d)",
                wrapper_data->config->vmtimeout, getpid(),getppid(), jvm_pid);
        sleep(wrapper_data->config->vmtimeout);
      
        jserv_error(JSERV_LOG_DEBUG,wrapper_data->config,
                "wrapper: watching processes every %d seconds(PID=%d,PPID=%d,JVM PID=%d)",
                wrapper_data->config->vminterval, getpid(),getppid(), jvm_pid);

        /* Check every ApJServVMInterval seconds */

        while (1) {
	  sighandler_t old_handler;

#ifndef JSERV_STANDALONE
            /* did parent httpd die? */
            if(getppid() == 1) {
                jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                            "wrapper: Apache exited, cleaning up (PID=%d)",
                            getpid());
                /* Send a signal to JVM */
                wrapper_shutdown(0);
                exit(0);
            }
#endif /* ifndef JSERV_STANDALONE */

			/* check that jvm is still running */

			if (jvm_pid != 0 && waitpid(jvm_pid, NULL, WNOHANG) != 0) {
		        jserv_error(JSERV_LOG_INFO, wrapper_data->config,
		                "wrapper: Java VM exited (PID=%d)",
		                getpid());
			    jvm_pid = 0;
			}


			/* install SIGALARM handler to implement a timeout in communication */
			/* NOTE: As it is installed in each loop, it should work with SYSV signal()*/
			old_handler = signal( SIGALRM, kill_hung_jvm);
			
			/* We use Ed's facility here to measure the timeout 
			   IMPORTANT: this is what we should expect from this variable name,
			   however we have to check if we really should share the same value
			   in many places.
			 */
			alarm(wrapper_data->config->vmtimeout);
		
			/* check if we can communicate to JVM */
			if( jvm_pid != 0 && jserv_protocol_function(wrapper_data->config->protocol,wrapper_data->config,
						    JSERV_PING,NULL) == JSERV_FUNC_COMMERROR) {

			  /* Note that it's harmless that this function is called twice
			     in case of timeout 
			  */
			  kill_hung_jvm(0);

			}


			/* Remove the signal, no matter if it was used or not */
			alarm(0);
			
			/* Now let's uninstall the handler, to avoid clashes if somebody
			   wanted to use it. */
			signal(SIGALRM, old_handler);

			/* set state if jvm is still alive */
			if (jvm_pid != 0)
				wrapper_restart_succeeded();
	
			/* try to restart jvm if it exited */
			if (jvm_pid == 0 && time(NULL) - last_restart > wrapper_restart_delay()) {
			
				if (wrapper_restart_want_debug_data()) {
					dump_debugging_info(arg,env);
				}

				/* notify of failure */
				wrapper_restart_failed();
            	
				/* check if we should just give up and exit */
				if (wrapper_restart_delay() == 0) {
				  jserv_error(JSERV_LOG_INFO, wrapper_data->config,
					  "wrapper: Java VM died too many times, exiting");
				  dump_debugging_info(arg, env);
				  exit(1);
				}
				
				/* check if new restart delay has elapsed */
				if (time(NULL) - last_restart > wrapper_restart_delay()) {
				     jserv_error(JSERV_LOG_INFO, wrapper_data->config,
					      "wrapper: Java VM restarting (PID=%d)", getpid());
				     jvm_pid = fork();
				     if (jvm_pid == 0) {
				       wrapper_exec_jserv_core(arg[0], arg, env);
				     }
				     last_restart = time(NULL);
				     jserv_error(JSERV_LOG_DEBUG, wrapper_data->config,
					      "wrapper: sleep config->vmtimeout %d (PID=%d)", wrapper_data->config->vmtimeout, getpid());
				     sleep(wrapper_data->config->vmtimeout);
                                     continue;
				}
			}

			/* delay before rechecking for jvm (jluc: hope it's the rigth place) */
                        jserv_error(JSERV_LOG_DEBUG, wrapper_data->config,
                                    "wrapper: sleep config->vminterval %d (PID=%d)", wrapper_data->config->vminterval, getpid());
			sleep(wrapper_data->config->vminterval);
        }
    }

    /* Log our CLASSPATH, for debugging purposes: */
    x=0;
    while (env[x] != NULL) {
        if (!strncmp(env[x],"CLASSPATH=",10)) {
            jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                    "wrapper classpath: %s",
                    env[x] + 10); /* 10 == strlen("CLASSPATH=") */
            break;
        }
        ++x;
    }

    if (env[x] == NULL) /* we didn't find CLASSPATH */
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
             "no classpath env variable set -- JServ class may not be found");
        
    wrapper_exec_jserv_core(arg[0], arg, env);
    return proc;
}

/* ========================================================================= */
/* Creates our Java Virtual Machine  */
int wrapper_create(wrapper_config *cfg) {

    /* Tell we're starting */
    /* We'll use the pid in the cleanup function in Apache 1.3. */
    wrapper_pid=wrapper_spawn();

    return 0;
}

/* ========================================================================= */
/* This is the cleanup function, used w/in Apache. */
int wrapper_destroy(wrapper_config *cfg) {
    int ret;
    if (wrapper_pid!=0) {
        ret = kill(wrapper_pid, SIGTERM);
    }
    return ret;
}

/* ========================================================================= */
/* This does the actual cleanup */
int wrapper_shutdown_core(wrapper_config *cfg) {
    if (jvm_pid != 0) {
        int counter = 0;
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
                    "wrapper: Terminating JServ (PID=%d, VM PID=%d)",
                    getpid(), jvm_pid);
        /* Send shutdown function */
        jserv_protocol_function(cfg->config->protocol,cfg->config,
                                JSERV_SHUTDOWN,NULL);
        /* Wait for child to go down */
        while (waitpid(jvm_pid,NULL,WNOHANG)==0) {
            /* give it a little while to shut down gracefully. Then kill it. */
            if (++counter > cfg->config->vmtimeout) {
                jserv_error(JSERV_LOG_EMERG, wrapper_data->config,
                            "wrapper: JServ (%d) didn't die nicely, killing it",
                            jvm_pid);
                kill(jvm_pid, SIGTERM); /* give the process a chance to die */
                counter = 0;
                while (counter++ < 3) {
                    if (waitpid(jvm_pid, NULL, WNOHANG) > 0)
                        return 0;
                    sleep(1);
                }
                if( waitpid(jvm_pid,NULL,WNOHANG)==0 ) {
                    kill(jvm_pid, SIGKILL);
                }
            }
            sleep(1);
        }
    }
    return 0;
}

/*****************************************************************************
 * Standalone wrapper code                                                   *
 *****************************************************************************/

/* ========================================================================= */
/* Wrapper standalone startup code */
void wrapper_standalone_startup(void) {
    pid_t mypid=fork();

    if (mypid < 0) {
        printf("Unable to fork (%s); exiting.\n",
                    strerror(errno));
        exit(1);
    } else if (mypid!=0) {
        printf("Apache JServ Standalone Wrapper - Going background\n");
        exit (0);
    }
}

/* ========================================================================= */
/* Wrapper standalone process (wait for user interruption) code */
void wrapper_standalone_process(void) {
    /* Wait to infinite... All is set up by signals */
    while (1) sleep(10000);
    exit (1);
}

#endif /* ifndef WIN32 */
