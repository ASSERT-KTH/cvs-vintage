/*****************************************************************************
 * Copyright (c) 1997-2000 The Java Apache Project. All rights reserved.     *
 *                                                                           *
 * Redistribution and use in source and binary forms, with or without        *
 * modification, are permitted provided that the following conditions are    *
 * met:                                                                      *
 *                                                                           *
 * 1. Redistributions of source code must retain the above copyright notice, *
 *    this list of conditions and the following disclaimer.                  *
 *                                                                           *
 * 2. Redistributions in binary form must reproduce the above copyright      *
 *    notice, this list of conditions and the following disclaimer in the    *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. Every modification must be notified to the "Java Apache Project"       *
 *    and redistribution of the modified code without prior notification is  *
 *    NOT permitted in any form.                                             *
 *                                                                           *
 * 4. All advertising materials mentioning features or use of this software  *
 *    must display the following acknowledgment:                             *
 *    "This product includes software developed by the Java Apache Project   *
 *     <http://java.apache.org>."                                            *
 *                                                                           *
 * 5. The names "JServ", "JServ Servlet Engine" and "Java Apache Project"    *
 *    must not be used to endorse or promote products derived from this      *
 *    software without prior written permission.                             *
 *                                                                           *
 * 6. Redistributions of any form whatsoever must retain the following       *
 *    acknowledgment:                                                        *
 *    "This product includes software developed by the Java Apache Project   *
 *     <http://java.apache.org>."                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY      *
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE       *
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR        *
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JAVA APACHE PROJECT OR ITS  *
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
 *                                                                           *
 * This software consists of voluntary contributions made by many            *
 * individuals on behalf of the Java Apache Project and was originally based *
 * on public domain software written by by Alexei Kosut <akosut@apache.org>. *
 * For more information on the Java Apache Project and the JServ Servlet     *
 * Engine project, please see <http://java.apache.org/>.                     *
 *****************************************************************************/

/*****************************************************************************
 * Description: protocol balancer, used to call local or remote jserv hosts  *
 * Author:      Bernard Bernstein <bernard@corp.talkcity.com>                *
 * Updated:     March 1999 Jean-Luc Rochat <jlrochat@jnix.com>               *
 * Description: solved part of fail-over problems & LB improvments           *
 * Version:     $Revision: 1.2 $
 *****************************************************************************/

#include "jserv.h"

#ifdef LOAD_BALANCE

/*****************************************************************************
 * Code for protocol balancer                                                *
 *****************************************************************************/

/* *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** */
/* The SESSION_ID is also defined in the Java. If one changes              */
/* then so must the other. This must stay in sync with the session cookie  */
/* or parameter set by the java code                                       */

#define SESSION_IDENTIFIER_JSERV "JServSessionId"
#define SESSION_IDENTIFIER_TOMCAT "JSESSIONID"
#define SESSION_IDENTIFIER_TOMCAT_PARAM "jsessionid"
#define ROUTING_IDENTIFIER "JSERV_ROUTE"

/* ========================================================================= */
/* Retrieve the parameter with the given name                                */
static char*
get_param(char *name, request_rec *r)
{
  char *pname = ap_pstrdup(r->pool, name);
  char *value = NULL;
  char *varg = NULL;
  int len = 0;

  pname = ap_pstrcat(r->pool, pname, "=", NULL);

/* commented out: original JServ code
  if (!r->args) {
    return NULL;
  }

  value = strstr(r->args, pname);
* end of original code */

/* XXX Will not work if ;jsessionid is not a path param for the last path component */
  value = strstr(r->uri, pname);

  if (value) {
    value += strlen(pname);
    varg = value;
    /* end of string or & */
    while (*varg && *varg != '&') {
      varg++;
      len++;
    }
  }

  if (len == 0) {
    return NULL;
  }

  return ap_pstrndup(r->pool, value, len);
}

/* ========================================================================= */
/* Retrieve the cookie with the given name                                   */
static char*
get_cookie(char *name, request_rec *r)
{
  const char *cookie;
  char *value;
  char *cname = ap_pstrdup(r->pool, name);

  cname = ap_pstrcat(r->pool, cname, "=", NULL);
  if ((cookie = ap_table_get(r->headers_in, "Cookie")) != NULL)
      /* ap_log_rerror(APLOG_MARK, APLOG_ALERT, r, "COOKIE: %s", cookie );	   */
    if ((value = strstr(cookie, cname)) != NULL) {
      char *cookiebuf, *cookieend;
      value += strlen(cname);
      cookiebuf = ap_pstrdup(r->pool, value);
      cookieend = strchr(cookiebuf, ';');
      if (cookieend)
        *cookieend = '\0';      /* Ignore anything after a ; */

      return cookiebuf;    /* Theres already a cookie, no new one */
    }

  return NULL;
}


/* ========================================================================= */
/* Retrieve session id from the cookie or the parameter                      */
/* (parameter first)                                                         */
/* We are here able to dispatch & maintain sessions on JServ and Tomcat      */
/*   */
/* ========================================================================= */
static char *
get_jserv_sessionid(request_rec *r, char *zone)
{
  char *val;
  char sessionid[256];

 /* first JServ 1.1 as it is the production one */
  strncpy(sessionid, SESSION_IDENTIFIER_JSERV, sizeof(sessionid)-1);
 /* as our strings are defined here we know they are < 256 bytes  */
 /* we check the routing info length */
  if (strlen(zone) < sizeof(sessionid)-strlen(sessionid)) 
      strcat(sessionid, zone);
  
  val = get_param(sessionid, r);

  if (val == NULL) {
      val = get_cookie(sessionid, r);

      if (val == NULL) {
         /* second JServ 1.0 as it is still used (no zone appended)*/
          strcpy(sessionid, SESSION_IDENTIFIER_JSERV);
          val = get_param(sessionid, r);

          if (val == NULL) {
              val = get_cookie(sessionid, r);

              if (val == NULL) {
                 /* 3rd Tomcat as nobody uses it in a LB production env yest */
                 /* Tomcat using parameter */
                  strcpy(sessionid, SESSION_IDENTIFIER_TOMCAT_PARAM);
                  val = get_param(sessionid, r);

                  if (val == NULL) {
                     /* Tomcat using parameter (uppercase) is it useful ? */
                     strcpy(sessionid, SESSION_IDENTIFIER_TOMCAT);
                     val = get_param(sessionid, r);
                  }
                  if (val == NULL) {
                     /* Tomcat using cookie */
                     strcpy(sessionid, SESSION_IDENTIFIER_TOMCAT);
                     val = get_cookie(sessionid, r);
                  }
              }
          }
      }
  }
  return val;
}

static int
get_jserv_session_balance(const char **hostid, request_rec *r, char *zone)
{
  char *sessionid = get_jserv_sessionid(r, zone);
  char *ch;

  if (sessionid == NULL)
    return 0;

  /*
   * Balance parameter is appended to the end
   */  
  ch = strrchr (sessionid, '.');
  if (ch == NULL)
    return 0;
  ch++;
  if (*ch == '\0')
    return 0;
  *hostid = ap_pstrdup(r->pool, ch);
  return 1;
}

/* ========================================================================= */
/* has to be done for every child during startup phase.                      */
/* Here we give every httpd process a privileged JServ target.               */
/* The goal is to have every httpd process beginning on a different JServ    */
/* so the load is balanced at startup time.                                  */
/* Every httpd process should always try to send requests to the same target.*/
/* This is also an easy entry point for socket keepalive as soon as it is    */
/* implemented.                                                              */
/* ========================================================================= */

int jserv_choose_default_target (jserv_config *cfg, jserv_request *req) {
    jserv_host *first;
    jserv_host *cur;
    int i;
    int jserv_nb=0;

    /* we count the jserv targets (get the modulo) */

    first = cur = req->mount->hosturls;
    while (cur) {
        cur = cur->next;
        jserv_nb++;
        if (cur == first) {
		break;
        }
    }
    if (!jserv_nb) {
        jserv_error(JSERV_LOG_EMERG,cfg,"balance:  %s",
                "virtual host not specified");
        return SERVER_ERROR;
    }
    /* we choose where this process will start in the circular list */
    /* we use the pid as random value. This works well on U**x.     */
    /* In fact any random like algorithm is good enough for us.     */
    /* NB: needs #httpd processes multiple of #targets for real LB  */

    cur = req->mount->hosturls;
    i = getpid() % jserv_nb;
    while (i--) {
            first = cur = cur->next;
    }
    req->mount->hosturls = req->mount->curr = cur;
    jserv_error(JSERV_LOG_DEBUG,cfg,
               "balance:  choosing %s:%d",
                cur->host, cur->port);
    return 0;
}

/* ========================================================================= */
/* Our request handler */
static int balance_handler(jserv_config *cfg, jserv_request *req, 
                          request_rec *r) {
    jserv_mount save;
    jserv_host *first;
    jserv_host *cur;
    int result;
    const char *hostid;


    /* debug message */
    jserv_error(JSERV_LOG_DEBUG,cfg,"balance: %d %s", getpid(),
                    "got another balance request");
    

    /* Check for correct config member */
    if (cfg==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"balance: %d %s", getpid(), 
                    "unknown configuration member for request");
        return SERVER_ERROR;
    }

    /* Check for correct jserv request member */
    if (req==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"balance: %s",
                    "null request not handled");
        return SERVER_ERROR;
    }
    if (req->mount==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"balance: %s",
                    "unknown mount for request");
        return SERVER_ERROR;
    }

    jserv_error(JSERV_LOG_DEBUG,cfg,"balance: zone %s for mountpoint %s",
                    req->mount->zone, req->mount->mountpoint);
    /* Check parameters and cookies for a current session */
    if (get_jserv_session_balance(&hostid, r, req->mount->zone) && hostid != NULL) {
        cur = cfg->hosturls;
        
        /* debug message */
        jserv_error(JSERV_LOG_INFO,cfg,
                   "balance: continuing session: %s",
                   hostid);
        
        /* found a session id, now get host with the given id */
        while (cur != NULL) {
          if (!strcmp(cur->id, hostid)) {
            /* found a matching host for the given id */
            save.hostaddr=req->mount->hostaddr;
            save.port=req->mount->port;
            save.host=req->mount->host;
            save.secretfile=req->mount->secretfile;
            save.secret=req->mount->secret;
            save.secretsize=req->mount->secretsize;
            req->mount->hostaddr = cur->hostaddr;
            req->mount->port = cur->port;
            req->mount->host = cur->host;
            req->mount->secretfile = cur->secretfile;
            req->mount->secret = cur->secret;
            req->mount->secretsize = cur->secretsize;
            
            if (r->subprocess_env) {
              ap_table_set(r->subprocess_env, ROUTING_IDENTIFIER, cur->id);
            }
            
            if (!jserv_isdead(cfg, cur)) {
              result = jserv_protocol_handler(cur->protocol, cfg, req, r);
              
              req->mount->hostaddr = save.hostaddr;
              req->mount->port = save.port;
              req->mount->host = save.host;
              req->mount->secretfile = save.secretfile;
              req->mount->secret = save.secret;
              req->mount->secretsize = save.secretsize;
            
              /* debug message */
              jserv_error(JSERV_LOG_INFO,cfg,
                       "balance: continuing to %s:%d",
                       cur->host, cur->port);

              if (result != SERVER_ERROR && result != HTTP_INTERNAL_SERVER_ERROR) {
		jserv_setalive(cfg, cur);
                return result;
              }
	      /* the server is alive but got an internal error */
	      if (r->status == SERVER_ERROR) {
		jserv_error(JSERV_LOG_ERROR,cfg,
		"balance: %d internal servlet error in server %s: %s://%s(%lx):%d",
		getpid(),
		cur->name, cur->protocol?cur->protocol->name:"DEFAULT", 
		cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);
		jserv_setalive(cfg, cur);
		return result;
	      }

              /* I noticed a non-responding server. I set it as non working in mmap'ed file */
              jserv_error(JSERV_LOG_INFO,cfg,
                   "balance: %d %s unsuccessfully ", getpid(), hostid);
              jserv_setdead(cfg, cur);
 	    }           
            /* if that server failed, break out and use regular load-balancing */
            break;
          }
          cur = cur->next;
        }
    }

    /* initialization for this zone */
    if (req->mount->curr==NULL) {
	jserv_choose_default_target (cfg, req);
    }
  
    /* find the next server that succeeds for fault tolerance */
    /* or simple load-balancing without session               */
    
    /* *** BB: We probably want some mechanism to take non-working servers */
    /* *** out of the loop temporarily and then retry in some interval     */
    /* *** rather than potentially check failing servers for every         */
    /* *** request.                                                        */
    /* *** But, even if we do that, it wouldn't work across all of the     */
    /* *** httpd processes. I don't know the best solution for this and    */
    /* *** I yield to others to solve that one.                            */
    
    /* *** JLR: I try here to give a solution                              */
    /* *** after a failure check if the JServ has been marked alive by     */
    /* *** another process (watchdog process)                              */
    /* *** we just try to read in the mmap'ed file                         */

    /* In tomcat CVS was: ifndef WIN32. Aparently fixed in jserv. */
#if 0
    /*
     * We never want to do keep the current server because in the following
     * scenario balancing will completely fail, overloading the first machine
     * that is responding; we use simple round-robin instead like in the
     * Win32 case.
     *
     * We have 10 jserv machines js1-10, and js1-5 are marked down, js6-10
     * are ok. 50% of the requests will go to the down machines, and each
     * one will try to find a working jserv, and ending up using js6. So
     * we end up with a load distribution:
     *
     *  js6 js7 js8 js9 js10
     *  60% 10% 10% 10% 10%
     */

    if (req->mount->curr != req->mount->hosturls) {
      if (jserv_isdead(cfg, req->mount->hosturls)) {
        /*keep the current target */
      }
      else {
        req->mount->curr = req->mount->hosturls;
      }
    }
#else
    /* on WIN32 we have one single process with n threads, so we can't   */
    /* use our "preferred target", we simply take the next target in our */
    /* circular list.                                                    */
    if (req->mount->curr->next) {
        req->mount->curr = req->mount->curr->next;
    }
#endif

    cur = first = req->mount->curr;
    do {
      if (jserv_isup(cfg, cur)) {
        /* debug message */
        jserv_error(JSERV_LOG_INFO,cfg,
                 "balance: %d attempting to connect to server %s: %s://%s(%lx):%d", getpid(), 
                 cur->name, cur->protocol?cur->protocol->name:"DEFAULT", 
                 cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);
      
        /* we don't insert the protocol or else the protocol would */
        /* not get switched back to "balance" automatically */
        save.hostaddr=req->mount->hostaddr;
        save.port=req->mount->port;
        save.host=req->mount->host;
        save.secretfile=req->mount->secretfile;
        save.secret=req->mount->secret;
        save.secretsize=req->mount->secretsize;
        req->mount->hostaddr = cur->hostaddr;
        req->mount->port = cur->port;
        req->mount->host = cur->host;
        req->mount->secretfile = cur->secretfile;
        req->mount->secret = cur->secret;
        req->mount->secretsize = cur->secretsize;
  
        if (r->subprocess_env) {
          ap_table_set(r->subprocess_env, ROUTING_IDENTIFIER, cur->id);
        }


        /* try this handler. If it fails, it will keep looking for a */
        /* server that works. If it succeeds, we return here */
        result = jserv_protocol_handler(cur->protocol, cfg, req, r);


        req->mount->hostaddr = save.hostaddr;
        req->mount->port = save.port;
        req->mount->host = save.host;
        req->mount->secretfile = save.secretfile;
        req->mount->secret = save.secret;
        req->mount->secretsize = save.secretsize;

        if (result != SERVER_ERROR && result != HTTP_INTERNAL_SERVER_ERROR) {
      
          /* debug message */
          jserv_error(JSERV_LOG_INFO,cfg,
             "balance: %d successfully made request to server %s: %s://%s(%lx):%d", getpid(),
             cur->name, cur->protocol?cur->protocol->name:"DEFAULT", 
             cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);

          jserv_setalive(cfg, cur);
          return result;
        }
	/* the server is alive but got an internal error */
	if (r->status == SERVER_ERROR) {
	    jserv_error(JSERV_LOG_ERROR,cfg,
	    "balance: %d internal servlet error in server %s: %s://%s(%lx):%d",
	    getpid(),
	    cur->name, cur->protocol?cur->protocol->name:"DEFAULT", 
	    cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);
	    jserv_setalive(cfg, cur);
	    return result;
	}
        /* I noticed a non-responding server. I set it as non working in mmap'ed file */
        /* we'll try the next one in the ring */
        jserv_error(JSERV_LOG_INFO,cfg,
             "balance: %d unsuccessfully ", getpid());
        jserv_setdead(cfg, cur);
      }
      else 
          jserv_error(JSERV_LOG_DEBUG,cfg,
             "balance: %d trying backup ", getpid());
      req->mount->curr = cur = cur->next;

    } while (cur != first);
    

    jserv_error(JSERV_LOG_ERROR,cfg,"balance: %s",
                "virtual host not found or not running");
    return SERVER_ERROR;
}

/* ========================================================================= */
/* We also attach (mmap) a file that will be used as shared (RW) memory.     */
/* between processes. If this fails (permissions, or mmap not supported), we */
/* just assume JServs are always alive (could be automatic mode).            */
/* This is to deal with fail-over problems.                                  */
/* And also can permit dynamic reconfiguration in the (near) future.         */
/* ========================================================================= */

int balance_init(jserv_config *cfg) {
    if (cfg->shmfile && cfg->shmfile != NULL) {
        if (!mmapjservfile(cfg, cfg->shmfile)) {
            jserv_error(JSERV_LOG_INFO,cfg,
               "balance: mmap errno=%d",
               errno);	
        }
	return watchdog_init(cfg);
    }
    return 0;
}
int balance_cleanup(jserv_config * cfg) {
        munmapjservfile();
	return watchdog_cleanup(cfg);
}

/*****************************************************************************
 * AJPv11 Protocol Structure definition                                      *
 *****************************************************************************/

jserv_protocol jserv_balancep = {
    "balance",                  /* Name for this protocol */
    8007,                       /* Default port for this protocol */
    balance_init,               /* init() */
    balance_cleanup,            /* cleanup() */
    NULL,                       /* child_init() */
    NULL,                       /* child_cleanup() */
    balance_handler,            /* handler() */
    NULL,                       /* function() */
    NULL,                       /* parameter() */
};

#endif
