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
 * Description: jserv_watchdog.c is used by jserv_balance.c to periodically  *
 *              poll dead jservs and udate their status in shared memory     *
 *              as soon they are accepting TCP connexions again.             *
 * Comments:                                                                 *
 * Author:      Jean-Luc Rochat <jlrochat@jnix.com>                          *
 * Updated:     June 1999 Bernard Bernstein <bernard@corp.talkcity.com>      *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/

#include "jserv.h"
#ifndef WIN32
#include <sys/signal.h>

/*****************************************************************************
 * Shared variables                                                          *
 * As these variables are pointers on common memory and not used for anything*
 * else, I do consider that threads can share them safely.                   * 
 * these variables are set by the main process, and shared by all httpd.     *
 *****************************************************************************/


/* private to watchdog. (set global for signal handler logging fct) */
jserv_config *watchdog_cfg = (jserv_config *)0;

static int jserv_ping (jserv_config *cfg, unsigned long address, unsigned short port);
void watchdog_shutdown (int sig);
/* ========================================================================= */
/* Signal handler for SIGTERM (15)                                           */
/* ========================================================================= */
void watchdog_shutdown (int sig) {
    if (watchdog_cfg)
    	jserv_error(JSERV_LOG_INFO, watchdog_cfg,
		"jserv_watchdog:(%d) watchdog_terminate()",
		 getpid());
    exit(0);
}

/* ========================================================================= */
/* We kill the process with a TERM signal                                    */
/* We wait for him to avoid a zombie                                         */
/* ========================================================================= */
#if defined(HAVE_MMAP) && !defined(WIN32)
int watchdog_cleanup (jserv_config *cfg) {
    int rc;
    int pid;

    if ((!cfg->shmfile) || (!*cfg->shmfile))
        return(0);

    jserv_error(JSERV_LOG_INFO,cfg,
                "jserv_watchdog:(%d) watchdog_cleanup()", getpid());


    if (!mmapjservfile(cfg, cfg->shmfile)) {
    	jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) no shared memory file", getpid());
	return 0;
    } 

    pid = jserv_getwatchdogpid();

    if (pid) {
        jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) killing %d ", getpid(), pid);
        kill (pid, SIGTERM);

        /* WIN32 hack we set the pid to 0 so the watchdog will have to exit */
        jserv_setwatchdogpid((pid_t)0);

        while (1) {
            rc = waitpid (pid, NULL, WNOHANG);
            if (rc == -1 && errno == EINTR)
                continue;
            /* this wait will fail for the very first call (errno ECHILD)     */
            /* apache starts a 1rst process (which forks the 1rst watchdog)   */
            /* then a 2nd, which has to kill the 1rst watchdog. but the 2nd   */
            /* apache is not the father. So we must not block on this.        */ 
            /* this works for the following restarts calls, as the apache     */
            /* main process is still the father of the followings watchdogs   */
            jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) wait pid %d OK(rc=%d errno=%d)", getpid(), pid, rc,errno);
            break;
        }
    }
    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d)return 0", getpid());
    return 0;
}
#else
int watchdog_cleanup (jserv_config *cfg) {
    return 0;
}
#endif
/* ========================================================================= */
/* We fork a child and send it in background                                 */
/* ========================================================================= */
#if defined(HAVE_MMAP) && !defined(WIN32)
int watchdog_init (jserv_config *cfg) {
    ShmHost Shmhost;
    ShmHost *host;
    pid_t mypid;
    int pid;

    mypid = getpid();

    if (cfg->shmfile && *cfg->shmfile) {
        jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) watchdog_init()", mypid);

        if (!mmapjservfile(cfg, cfg->shmfile)) {
    		jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) _host is null", mypid);
		return 0;
	} 
	
	if ((pid=fork()!=(pid_t)0)) {
    	    jserv_error(JSERV_LOG_DEBUG,cfg,"watchdog:(%d) %d forked", mypid, pid);
	    return 0;	/* father */
	}
	else {
	    signal(SIGTERM, watchdog_shutdown);
            watchdog_cfg = cfg;

	    mypid = getpid();
	    jserv_setwatchdogpid(mypid);
  	    for (;jserv_getwatchdogpid() == mypid;) {
		sleep(cfg->vminterval);

    		jserv_error(JSERV_LOG_DEBUG,cfg,"watchdog:(%d) wakeup", mypid);
	 	host = jserv_get1st_host(&Shmhost);
	        while  (host != (ShmHost *)0) {
    	            if (jserv_getwatchdogpid() != mypid) 
		        break;
   		    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) state = %c  %s", mypid, host->state, host->name);
	  	    switch  (host->state) {
		        case UP: 
			    break;
	    	        case DOWN:
   			    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) state = %c ping  %s", mypid, host->state, host->name);

			    /* is it still dead ? */
			    if (!jserv_ping (cfg, host->ip, host->port)) {
    	                        if (jserv_getwatchdogpid() != mypid) 
		                   break;
			  	jserv_changeexistingstate(host->name, (char *)"-", (char)'+');;
			    }
   			    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) state = %c after ping  %s", mypid, host->state, host->name);
			    break;
	  	        case SHUTDOWN_IMMEDIATE: 
		        case SHUTDOWN_GRACEFUL: 
			    /* it's an admin task that shut this JServ */
			    /* so it's not our job to go against */
			    break;
		        default :
    			    jserv_error(JSERV_LOG_INFO,cfg,"jserv_watchdog:(%d) state = %c file corrupted", mypid, host->state);
			    exit(0);
	            }
	 	    host = jserv_getnext_host(host);
	        }
	    }
        }
        jserv_error(JSERV_LOG_DEBUG,cfg,"watchdog:(%d) done", mypid);
    }
    exit (0);
}	
#else
int watchdog_init (jserv_config *cfg) {
    return 0;
}
#endif

static int jserv_ping (jserv_config *cfg, unsigned long address, unsigned short port) {
	int s;
	int ret;
	struct sockaddr_in addr; 
	int mypid;
	mypid = getpid();

    	jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_ping:(%d) creating socket", mypid);
	if ((s = socket (AF_INET,SOCK_STREAM, IPPROTO_TCP)) == -1) {
    		 jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_watchdog:(%d) can't create socket", mypid);
		 return -1;
	}

        addr.sin_addr.s_addr = address;
        addr.sin_port = (unsigned short int)htons(port);
        addr.sin_family = AF_INET;

	while (1) {
    	    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_ping:(%d) connecting", mypid);

            if ((ret = connect (s, (struct sockaddr *) &addr, sizeof(addr))) == -1)
		if (errno == EINTR)
			continue;
	    break;
	}	
    	jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_ping:(%d) result = %d", mypid, ret);
	close (s);
	if (!ret) {
		return 0;
	}
	return -1;
}
#else

int watchdog_cleanup (jserv_config *cfg) {
	return 0;
}
int watchdog_init (jserv_config *cfg) {
	return 0;
}
#endif
