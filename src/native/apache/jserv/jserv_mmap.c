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
 * Description: jserv_mmap.c is used by jserv_balance.c to share server's    *
 *              state and other variables.                                   *
 *              This avoids sending requests to one JServ if another         *
 *              process found it was not present. (fail-over part)           *
 * Comments:    MT-unsafe                                                    *
 * Author:      Jean-Luc Rochat <jlrochat@jnix.com>                          *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/

#include "jserv.h"

#if defined(HAVE_MMAP) && !defined(WIN32)
/* ========================================================================= */
/* we are on a system with the mmapp system call.                            */
/* mmap'ed file is a shared memory read/write by all processes.              */
/* ========================================================================= */


struct shared_hdr {
        char ident[16];
        pid_t watchdog_pid;
        int nb;
};

struct shared_host {
        char state;
        char name[64];
        unsigned long hostaddr;
        short port;

};

struct shm_mmaped_file {
        struct shared_hdr hdr;
        struct shared_host hostarray[1];
};


#include <sys/mman.h>
/*****************************************************************************
 * Shared variables                                                          *
 * These variables are pointers on common memory                             *
 *****************************************************************************/
caddr_t shmf;
struct shm_mmaped_file * _shmf;
struct shared_hdr * _hdr;
struct shared_host * _host;

struct stat filestat;
void jserv_dbgshm(jserv_config *cfg);

/* ========================================================================= */
/* We overwrite(or just fill) the file and initialize datas                  */
/* ========================================================================= */
static void create_shmfile (jserv_config *cfg, int fd) {
        int i, nb;
	char *ptr;
	struct shared_hdr h;
	struct shared_host js[NB_MAX_JSERVS];

	for  (ptr=(char *)&h, i=0; i< sizeof(h); i++)
		ptr[i]='$';
	for  (ptr=(char *)&js, i=0; i< sizeof(js); i++)
		ptr[i]='$';
        jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:writing ");
	
	strcpy (h.ident, "jserv_mmap");
	h.watchdog_pid = 0;
	h.nb = 0;
        if ((nb = write (fd, &h, sizeof(h)) != sizeof(h))) {
	    /* error */
            jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap:error writing ");
        }
	for (i=0;i<NB_MAX_JSERVS;i++) {
	    js[i].state = DOWN;
	    js[i].name[0] = '\0';
	}
        if ((nb = write (fd, js, sizeof(js)) != sizeof(js))) {
	    /* error */
            jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap:error writing ");
        }
}
        
	

/* ========================================================================= */
/* unmmaps a file                                                            */ 
/* ========================================================================= */
void munmapjservfile()
{
    if (shmf) {
        munmap((caddr_t)shmf, filestat.st_size);
        shmf = (caddr_t) 0;
        _shmf = (struct shm_mmaped_file *) 0;
    }
}

/* ========================================================================= */
/* mmaps a file                                                              */ 
/* ========================================================================= */


static struct shm_mmaped_file *  _mmapjservfile(jserv_config *cfg, char *filename) {
    int fd;

    if (_shmf) {
        jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) remmaping", getpid());
        return _shmf;
	/*
        munmapjservfile();
        shmf = 0;
        _shmf = 0;
        _host = 0;
        _hdr = 0;
        */
    }
   
    jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) open %s ", getpid(), filename);

    ap_block_alarms();
    fd = open(filename, O_RDWR|O_CREAT, 0777);
    if (fd == -1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: cant open %s errno=%d", filename, errno);
        return NULL;
    }
    if (stat(filename, &filestat) == -1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: unable to stat %s", filename);
        return NULL;
    }
    if (filestat.st_size == 0) {
	create_shmfile (cfg, fd);
        if (stat(filename, &filestat) == -1) {
            jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: unable to stat %s", filename);
	}
    }
    shmf = mmap(NULL, filestat.st_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (shmf == (caddr_t)-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: can't mmap %s errno=%d", filename, errno);
	close(fd);
	ap_unblock_alarms();         
        return NULL;
    }
    close(fd);
    ap_unblock_alarms();

    if (filestat.st_size < sizeof(struct shared_hdr)+ (NB_MAX_JSERVS*sizeof (struct shared_host))) {
        jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: file %s sz is too small corrupted ?", filename);
        return NULL;
    } 
    if (shmf && (strcmp(shmf, "jserv_mmap"))) {
        jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap: file %s is corrupted", filename);
        return NULL;
    } 
    _shmf = (struct shm_mmaped_file *) shmf;
    _hdr = (struct shared_hdr *) &_shmf->hdr;
    _host = (struct shared_host *)_shmf->hostarray;
    return _shmf;

}

/* Here we map a structure over the mmaped file */


static struct shared_host * jserv_addhost(jserv_host *cur) {
	struct shared_host *host;
	int nbcurr = 0;
	host = (struct shared_host *)_host;

	/* begin critical section (not MT safe)*/
	if (_hdr->nb < NB_MAX_JSERVS) {
            nbcurr = _hdr->nb++; 
	}
	/* end critical section */
	else return (struct shared_host *)0;

        /* we can live with the following  ouside a mutex  */
        /* just because the only risk is to get twice the  */
        /* same JServ in the shm. In that case the watchdog*/
        /* would ping it 2* until finding it alive, but    */
        /* nobody else would access  the 2nd entry.        */

        host = (struct shared_host *) &_host[nbcurr];
        
	host->state = DOWN;
	host->hostaddr = cur->hostaddr;
	host->port = cur->port;
	strncpy (host->name, cur->id, sizeof(host->name));

	return host;
}

static struct shared_host * jserv_gethost(char *id) {
	struct shared_host *host;
	int nbcurr;
	host = (struct shared_host *)_host;
	nbcurr = 0;

	while  (nbcurr < _hdr->nb ) {
   	    nbcurr++;

  	    switch  (host->state) {
		case DOWN:
		case SHUTDOWN_IMMEDIATE: 
		case SHUTDOWN_GRACEFUL: 
		case UP: 
			break;
		default :
			return (struct shared_host *) 0; 
			break;
            }

	    if (!strcmp(host->name, id))
		return (host);
	    host++;
	}
	return (struct shared_host *) 0; 
}

char jserv_getstate(jserv_config *cfg, jserv_host *cur) {
	struct shared_host  * host;
	if (!shmf)
 	   return 0;
	host = jserv_gethost(cur->id);
	if (host == 0)
            return 0;
	return (host->state);	
}
int jserv_isup(jserv_config *cfg, jserv_host *cur) {
	struct shared_host  * host;
	if (!shmf)
 	   return 1;
	host = jserv_gethost(cur->id);
	if (host == 0)
            return 1;
	return (host->state == UP);	
}
int jserv_isdead(jserv_config *cfg, jserv_host *cur) {
	struct shared_host  * host;
	if (!shmf)
 	   return 0;
	host = jserv_gethost(cur->id);
	if (host == 0)
            return 0;
	return (host->state == DOWN);	
}

void jserv_changeexistingstate(char *id, char *fromstates, char newstate) {
	struct shared_host  * host;
	if (!shmf)
 	   return; 
  	switch  (newstate) {
		case DOWN:
		case SHUTDOWN_IMMEDIATE: 
		case SHUTDOWN_GRACEFUL: 
		case UP: 
			break;
		default :
			return ;
			break;
        }
	host = jserv_gethost(id);

	if (host == 0) {
	    return;
	}
        if (strchr(fromstates, host->state))
            host->state = newstate;
}


void jserv_changestate(jserv_config *cfg, jserv_host *cur, char *fromstates, char newstate) {
	struct shared_host  * host;
	if (!shmf)
 	   return; 
  	switch  (newstate) {
		case DOWN:
		case SHUTDOWN_IMMEDIATE: 
		case SHUTDOWN_GRACEFUL: 
		case UP: 
			break;
		default :
			return ;
			break;
        }
	host = jserv_gethost(cur->id);
	if (host == 0)
		host = jserv_addhost(cur);

	if (host == 0) {
	    jserv_error(JSERV_LOG_EMERG,cfg,"jserv_mmap:(%d) JServ table full", getpid());
	    return;
	}

        if (strchr(fromstates, host->state))
            host->state = newstate;

	host->hostaddr = cur->hostaddr;
	host->port = cur->port;
}

void jserv_setalive(jserv_config *cfg, jserv_host *cur) {
        /* we do not override admin commands (state=SHUTDOWN*) */
        jserv_changestate(cfg, cur, "-", '+');
}

void jserv_setdead(jserv_config *cfg, jserv_host *cur) {
        /* we do not override admin commands (state=SHUTDOWN*) */
        jserv_changestate(cfg, cur, "+", '-');
}

void jserv_dbgshm(jserv_config *cfg) {
	struct shared_host *host;
	int nbcurr;
	host = (struct shared_host *)_host;
	nbcurr = 0;

    	jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) ---------------", getpid());
	if (!_host) {
    		jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) _host is null", getpid());
		return;
	} 

	while  (nbcurr++ < _hdr->nb ) {
	    switch  (host->state) {
		case DOWN:
		case SHUTDOWN_IMMEDIATE: 
		case SHUTDOWN_GRACEFUL: 
		case UP: 
    			jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) state = %c  name = %s", getpid(), host->state, host->name);
			break;
		default :
    			jserv_error(JSERV_LOG_DEBUG,cfg,"jserv_mmap:(%d) state = %c file corrupted", getpid(), host->state);
			return;
	    }
	    host++;
	}
	return;
}	

void jserv_setwatchdogpid(pid_t pid) {
    if (_hdr)
        _hdr->watchdog_pid=pid;
}

pid_t jserv_getwatchdogpid() {
    if (_hdr)
        return(_hdr->watchdog_pid);
    return (-1);
}

int mmapjservfile(jserv_config *cfg, char *filename) {
    return ( _mmapjservfile(cfg, filename)==NULL?0:1);
}

ShmHost * jserv_get1st_host(ShmHost *shmhost) {
    shmhost->opaque= 0;
    return jserv_getnext_host(shmhost);;
}

ShmHost * jserv_getnext_host(ShmHost *shmhost) {
    struct shared_host *host;
    int nbcurr;
    host = (struct shared_host *)_host;
    nbcurr = 0;

    while  (nbcurr < _hdr->nb ) {
        if (shmhost->opaque == nbcurr) {
            strncpy(shmhost->name, host->name, sizeof(shmhost->name)-1); 
            shmhost->state =  host->state;
            shmhost->ip = host->hostaddr;
            shmhost->port= host->port;
            shmhost->opaque += 1;
            return shmhost;
        }
        nbcurr++;
        host++;
    }
    return 0;
}

#else

/* ========================================================================= */
/* we are on a system without the mmapp                                      */
/* these datas won't be shared by other processes.                           */
/* ========================================================================= */

int mmapjservfile(jserv_config *cfg, char *filename) {
        return  0;
}

void munmapjservfile() { 
}

pid_t jserv_getwatchdogpid() {
    return 0;
}

void jserv_setwatchdogpid(pid_t pid) {
}


void jserv_changeexistingstate(char *id, char *fromstates, char tostate) {
}

void jserv_changestate(jserv_config *cfg, jserv_host *cur, char *fromstates, char tostate) {
}

void jserv_setalive(jserv_config *cfg, jserv_host *cur) {
}

void jserv_setdead(jserv_config *cfg, jserv_host *cur) {
}

int jserv_isup(jserv_config *cfg, jserv_host *cur) {
	return 1;
}

int jserv_isdead(jserv_config *cfg, jserv_host *cur) {
	return 0;
}

char jserv_getstate(jserv_config *cfg, jserv_host *cur) {
    return ('+');
}

ShmHost * jserv_get1st_host(ShmHost *host) {
    return (ShmHost *)0;
}

ShmHost * jserv_getnext_host(ShmHost *host) {
    return (ShmHost *)0;
}

void jserv_dbgshm(jserv_config *cfg) {
}
#endif

/* ========================================================================= */
/* unmmaps a file                                                            */ 
/* ========================================================================= */

