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
 * Description: mod_jserv.c is used by Apache to talk to Apache JServ.       *
 *              It provides a common entry point for protocols and runs      * 
 *              configuration, initialization and request tasks.             *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.2 $                                             *
 *****************************************************************************/
#include "jserv.h"

/*****************************************************************************
 * Shared variables                                                          *
 *****************************************************************************/
jserv_config *jserv_servers=NULL;
pool *jserv_pool=NULL;

/*****************************************************************************
 * Configuration Procedures                                                  *
 *****************************************************************************/

#ifdef LOAD_BALANCE
/* ========================================================================= */
/* Check the balance structures and fill in defaults */
static void jserv_balance_config_default(pool *p, jserv_config *cfg) {
  jserv_balance *curbal = cfg->balancers;
  jserv_host *curhost = cfg->hosturls;
  
  while (curbal != NULL) {
    if (curbal->weight == JSERV_DEFAULT) {
      curbal->weight = 1;
    }
    jserv_error(JSERV_LOG_INFO,cfg,"setting defaults for balance %s-%s (weight %d)",
        curbal->name, curbal->host_name, curbal->weight);

    curbal = curbal->next;
  }
  
  while (curhost != NULL) {
    if (curhost->id == NULL) {
      curhost->id = curhost->name;
    }
    if (curhost->protocol==NULL) curhost->protocol=cfg->protocol;
    if (curhost->host==NULL) {
        curhost->host=cfg->host;
        curhost->hostaddr=cfg->hostaddr;
    }
    if (curhost->port==JSERV_DEFAULT) curhost->port=cfg->port;
    if (curhost->secretfile==NULL) {
        curhost->secretfile=cfg->secretfile;
        curhost->secret=cfg->secret;
        curhost->secretsize=cfg->secretsize;
    }
    jserv_error(JSERV_LOG_INFO,cfg,"setting defaults for host %s-%s",
        curhost->name, curhost->id);
    curhost = curhost->next;
  }
}
#endif



/* ========================================================================= */
/* Check our mount default config */
static void jserv_mount_config_default(pool *p, jserv_config *cfg) {

    jserv_mount *cur=cfg->mount;

#ifdef LOAD_BALANCE
    /* *** BB: I don't know if this is the right place to do this. */
    /* *** this may happen more often than we need, but it has worked for me so far */
    /* *** perhaps someone more familiar with the overall flow can help drop this */
    /* *** into the appropriate location */
    jserv_balance_config_default(p, cfg);
#endif  

    while (cur!=NULL) {
        /* Check mount point */
        if (cur->mountpoint==NULL) jserv_error_exit(JSERV_LOG_EMERG,cfg,
                        "Mountpoint not defined in mount structure");
        /* Update configuration pointer */
        cur->config=cfg;

        /* Check defaults */
        if (cur->protocol==NULL) cur->protocol=cfg->protocol;
        if (cur->host==NULL) {
            cur->host=cfg->host;
            cur->hostaddr=cfg->hostaddr;
        }
        if (cur->port==JSERV_DEFAULT) cur->port=cfg->port;
        if (cur->secretfile==NULL) {
            cur->secretfile=cfg->secretfile;
            cur->secret=cfg->secret;
            cur->secretsize=cfg->secretsize;
        }
        
#ifdef LOAD_BALANCE
        /* *** BB: This is fairly tightly integrated. It could be argued that */
        /* *** This whole load-balancing system really belongs in a separate */
        /* *** configuration than the existing mount system, but I feel that */
        /* *** expanding the current mount functionality will be more familiar */
        /* *** to those who want to add load-balancing to an existing configuration */
        
        /* construct the host balancers from the matching configurations */
        cur->hosturls = NULL;
        
        /* if the protocol is 'balance', create the list of actual */
        /* mountpoints for this mount struct */
        if (!strcmp(cur->protocol->name, "balance")) {
        
          /* find the balancers that have this hostname */
          jserv_balance *curbal = cfg->balancers;

          jserv_error(JSERV_LOG_INFO,cfg,"setting up balance for mount %s, zone %s",
            cur->mountpoint, cur->zone);
        
          while (curbal != NULL) {
            if (!strcmp(curbal->name, cur->host)) {
              /* found a matching balancer, now find its matching host */
              jserv_host *curhost = cfg->hosturls;
              while (curhost != NULL) {
                if (!strcmp(curbal->host_name, curhost->name)) {
                  jserv_host *newhost;
                  int i;
                  /* overly simple way of handling weight */
                  /* this simply makes n copies of the same host according to */
                  /* its weight, these should be either more interspersed or */
                  /* use some other method for calculating weighting */
                  
                  for (i=0; i<curbal->weight; i++) {
                    newhost = (jserv_host *) ap_pcalloc(p,sizeof(jserv_host));
                    
                    /* copying the current host to this new one */
                    /* we're reusing the same strings since they should */
                    /* never change and should be in the same memory pool */
                    /* ??? ARE THEY IN THE SAME MEMORY POOL? *** */
                    newhost->name = curhost->name;
                    newhost->id = curhost->id;
                    newhost->config = curhost->config;
                    newhost->protocol = curhost->protocol;
                    newhost->host = curhost->host;
                    newhost->hostaddr = curhost->hostaddr;
                    newhost->port = curhost->port;
                    newhost->secretfile = curhost->secretfile;
                    newhost->secret = curhost->secret;
                    newhost->secretsize = curhost->secretsize;

                    jserv_error(JSERV_LOG_INFO,cfg,"balancing host %s-%s",
                        curhost->name, curhost->id);

                    /* look around the ring until we find the last host */
                    if (cur->hosturls!=NULL) {
                      jserv_host *firsthost;
                      jserv_host *curmnthost = firsthost = cur->hosturls;
                      while (curmnthost->next != firsthost)
                        curmnthost=curmnthost->next;
                      curmnthost->next = newhost;
                    }
                    else {
                      cur->hosturls = newhost;
                    }
                    
                    /* close the ring */
                    newhost->next = cur->hosturls;

                  }
                }
                curhost = curhost->next;
              }
              
            }
            curbal = curbal->next;
          }
        }
#endif
        cur=cur->next;
    }
}

/* ========================================================================= */
/* Check our server default config */
static void jserv_server_config_default(pool *p, jserv_config *cfg) {

    /* Check ApJServManual */
    if (cfg->manual==JSERV_DEFAULT) cfg->manual=JSERV_DEFAULT_MANUAL;

    /* Check ApJServProperties */
    if (cfg->properties==NULL)
        cfg->properties=ap_pstrdup(p,JSERV_DEFAULT_PROPERTIES);

    /* Check ApJServDefaultProtocol */
    if (cfg->protocol==NULL) {
        cfg->protocol=jserv_protocol_getbyname(JSERV_DEFAULT_PROTOCOL);
        if (cfg->protocol==NULL) jserv_error_exit(JSERV_LOG_EMERG,cfg,
            "Cannot find ApJServDefaultProtocol %d", JSERV_DEFAULT_PROTOCOL);
    }

    /* Check ApJServDefaultPort */
    if (cfg->port==JSERV_DEFAULT) cfg->port=cfg->protocol->port;

    /* Check ApJServDefaultHost */
    if (cfg->host==NULL) {
        cfg->host=ap_pstrdup(p,JSERV_DEFAULT_HOST);
        cfg->hostaddr=JSERV_DEFAULT;
    } 

    /* Check address for ApJServDefaultHost */
    if (cfg->hostaddr==JSERV_DEFAULT) cfg->hostaddr=jserv_resolve(cfg->host);
    if (cfg->hostaddr==0) {
        jserv_error_exit(JSERV_LOG_EMERG,cfg,
            "Error setting defaults: ApJServDefaultHost name \"%s\" can't be resolved",
            cfg->host);
    }

    /* Check ApJServMountCopy */
    if (cfg->mountcopy==JSERV_DEFAULT) cfg->mountcopy=JSERV_DEFAULT_MOUNTCOPY;

    /* Check ApJServLogFile */
    if (cfg->logfile==NULL) {
        cfg->logfile=ap_pstrdup(p,JSERV_DEFAULT_LOGFILE);
        cfg->logfilefd=JSERV_DEFAULT;
    }

    /* Check ApJServLogLevel */
    if (cfg->loglevel==JSERV_DEFAULT) {
        cfg->loglevel=APLOG_DEBUG;
    }

    /* Check file descriptor for ApJServLogFile */
    if (cfg->logfilefd==JSERV_DEFAULT) {
        const char *buf=jserv_openfile(p, cfg->logfile, JSERV_TRUE, 
                    &cfg->logfilefd, JSERV_LOGFILE_FLAGS, JSERV_LOGFILE_MODE);
        if (buf!=NULL) jserv_error_exit(JSERV_LOG_EMERG,cfg,
                            "Error setting defaults: ApJServLogFile: %s", buf);
    }

    /* Fill our ApJServMount structures */ 
    jserv_mount_config_default(p, cfg);

}


/* ========================================================================= */
/* Create our server config */
static void *jserv_server_config_create(pool *p, server_rec *s) {
    jserv_config *cfg=(jserv_config *)ap_pcalloc(p, sizeof(jserv_config));

    /* Our server entry */
    cfg->server=s;

    /* DEFAULT values setup */
    cfg->manual=JSERV_DEFAULT;
    cfg->properties=NULL;
    cfg->protocol=NULL;
    cfg->host=NULL;
    cfg->hostaddr=JSERV_DEFAULT;
    cfg->port=JSERV_DEFAULT;
    cfg->mount=NULL;
    cfg->mountcopy=JSERV_DEFAULT;
    cfg->logfile=NULL;
    cfg->logfilefd=JSERV_DEFAULT;
    cfg->loglevel=JSERV_DEFAULT;
    cfg->secretfile=NULL;
    cfg->secret=NULL;
    cfg->secretsize=JSERV_DEFAULT;
    cfg->actions=ap_make_table(p,5);
#ifdef LOAD_BALANCE
    cfg->balancers=NULL;
    cfg->hosturls=NULL;
    cfg->shmfile=NULL;
#endif

    /* Add our server to server chain (if it's not virtual) */
    if (!(s->is_virtual)) {
        cfg->next=NULL;
        jserv_servers=cfg;
    }

    cfg->retryattempts=0;
    cfg->vminterval=JSERV_DEFAULT_VMINTERVAL;
    cfg->vmtimeout=JSERV_DEFAULT_VMTIMEOUT;
    cfg->envvars=ap_make_table(p,0);

    /* We created the server config */
    return(cfg);
}

/* ========================================================================= */
/* Merge two different server configs */
static void *jserv_server_config_merge(pool *p, void *vbase, void *voverride) {
    jserv_config *cfg=(jserv_config *)ap_pcalloc(p, sizeof(jserv_config));
    jserv_config *base=(jserv_config *) vbase;
    jserv_config *override=(jserv_config *) voverride;
    int copy;

    /* Setup base server defaults */
    jserv_server_config_default(p, base);

    /* Our server entry */
    cfg->server=override->server;

    /* Things not handled inside VIRTUALHOST */
    cfg->manual=base->manual;
    cfg->properties=base->properties;

    /* Configuration duplication */
    cfg->protocol=override->protocol?
                  override->protocol:
                  base->protocol;
    cfg->port=(override->port!=JSERV_DEFAULT)?
               override->port:
               cfg->protocol->port;
    cfg->mountcopy=(override->mountcopy!=JSERV_DEFAULT)?
                    override->mountcopy:
                    base->mountcopy;

    /* ApJServDefaultHost merging */
    if (override->host!=NULL) {
        cfg->host=override->host;
        cfg->hostaddr=override->hostaddr;
    } else {
        cfg->host=base->host;
        cfg->hostaddr=base->hostaddr;
    }

    /* ApJServLogFile merging */
    if (override->logfile!=NULL) {
        cfg->logfile=override->logfile;
        cfg->logfilefd=override->logfilefd;
    } else {
        cfg->logfile=base->logfile;
        cfg->logfilefd=base->logfilefd;
    }

    /* ApJServLogLevel merging */
    if (override->loglevel!=JSERV_DEFAULT)
	cfg->loglevel=override->loglevel;
    else
	cfg->loglevel=base->loglevel;

    /* ApJServSecretKey merging */
    if (override->secretfile!=NULL) {
        cfg->secretfile=override->secretfile;
        cfg->secret=override->secret;
        cfg->secretsize=override->secretsize;
    } else {
        cfg->secretfile=base->secretfile;
        cfg->secret=base->secret;
        cfg->secretsize=base->secretsize;
    }

    /* Check whether to copy or not base mounts */
    copy=JSERV_FALSE;
    if (override->mountcopy==JSERV_FALSE) copy=JSERV_FALSE;
    if (override->mountcopy==JSERV_TRUE) copy=JSERV_TRUE;
    if (override->mountcopy==JSERV_DEFAULT) {
        if (base->mountcopy==JSERV_FALSE) copy=JSERV_FALSE;
        if (base->mountcopy==JSERV_TRUE) copy=JSERV_TRUE;
    }
    /* Copy mounts if nedeed */
    cfg->mount=override->mount;
    cfg->balancers=override->balancers;
    cfg->hosturls=override->hosturls;
    cfg->shmfile=override->shmfile;

    if (copy==JSERV_TRUE) {
        if (cfg->mount==NULL) cfg->mount=base->mount;
        else {
            jserv_mount *cur=cfg->mount;

            while (cur->next!=NULL) cur=cur->next;
            cur->next=base->mount;
        }
#ifdef LOAD_BALANCE
        if (cfg->hosturls==NULL) cfg->hosturls=base->hosturls;
        else {
            jserv_host *cur=cfg->hosturls;

            while (cur->next!=NULL) cur=cur->next;
            cur->next=base->hosturls;
        }

        if (cfg->shmfile==NULL) cfg->shmfile=base->shmfile;

        if (cfg->balancers==NULL) cfg->balancers=base->balancers;
        else {
            jserv_balance *cur=cfg->balancers;

            while (cur->next!=NULL) cur=cur->next;
            cur->next=base->balancers;
        }
#endif
    }
    /*A&M changes made to export env vars defined in virtual host (like
https) */
    cfg->envvars = base->envvars;

    if (!ap_is_empty_table(override->envvars)) {
        int i;
        array_header *hdr_arr;
        table_entry *elts;

        hdr_arr = ap_table_elts(override->envvars);
        elts = (table_entry *) hdr_arr->elts;

        for (i = 0; i < hdr_arr->nelts; ++i) {
            if (!elts[i].key) continue;
            if (!elts[i].val) continue;

            ap_table_add(cfg->envvars, ap_pstrdup(p,elts[i].key),
ap_pstrdup(p,elts[i].val));

            /* A&M
            jserv_error(JSERV_LOG_INFO,cfg,"ajp12: added env var %s %s
",elts[i].key, elts[i].val);
            */
        }
    }

    /* end changes made by us */

    /* Fill mount defaults */
    jserv_mount_config_default(p, cfg);

    /* Merge action tables */
    cfg->actions=ap_overlay_tables(p, override->actions, base->actions);

    /* Add our server to server chain */
    if (jserv_servers==NULL) {
        /* Should never happen but I want to be sure */
        cfg->next=NULL;
        jserv_servers=cfg;
    } else {
        jserv_config *cur=jserv_servers;

        /* Append configuration to list */
        while(cur->next!=NULL) cur=cur->next;
        cur->next=cfg;
        cfg->next=NULL;
    }
    cfg->retryattempts = base->retryattempts;
    cfg->vminterval = base->vminterval;
    cfg->vmtimeout = base->vmtimeout;

    /* All done */
    return (cfg);
}

/* ========================================================================= */
/* Handle ApJServManual directive (FLAG) */
static const char *jserv_cfg_manual(cmd_parms *cmd, void *dummy, int flag) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check if we already processed ApJServManual directives */
    if (cfg->manual!=JSERV_DEFAULT)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Check if we specified this in a virtual server */
    if (cfg->server!=NULL)
        if (cfg->server->is_virtual)
            return ap_pstrcat(cmd->pool, cmd->cmd->name,
                              ": cannot be specified inside <VirtualHost>",
                              NULL);

    /* Set up our value */
    cfg->manual=flag?JSERV_TRUE:JSERV_FALSE;

    return NULL;
}

/* ========================================================================= */
/* Handle ApJServShmFile directive (TAKE1) */
static const char *jserv_cfg_shmfile(cmd_parms *cmd, void *dummy, 
                                     char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check if we already processed ApJServLogFile directives */
    if (cfg->shmfile!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);


    /* Check if we specified this in a virtual server */
    if (cfg->server!=NULL)
        if (cfg->server->is_virtual)
            return ap_pstrcat(cmd->pool, cmd->cmd->name,
                              ": cannot be specified inside <VirtualHost>",
                              NULL);

    /* Set up our value */
    cfg->shmfile=ap_server_root_relative(cmd->pool,value);
    return NULL;
}
/* ========================================================================= */
/* Handle ApJServProperties directive (TAKE1) */
static const char *jserv_cfg_properties(cmd_parms *cmd, void *dummy,
                                        char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check if we already processed ApJServProperties directives */
    if (cfg->properties!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Check if we specified this in a virtual server */
    if (cfg->server!=NULL)
        if (cfg->server->is_virtual)
            return ap_pstrcat(cmd->pool, cmd->cmd->name,
                              ": cannot be specified inside <VirtualHost>",
                              NULL);

    /* Set up our value */
    cfg->properties=ap_server_root_relative(cmd->pool,value);
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServDefaultProtocol directive (TAKE1) */
static const char *jserv_cfg_protocol(cmd_parms *cmd, void *dummy, 
                                      char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check we did not provided status or wrapper protocols */
    if ((strcasecmp(value,"status")==0) || (strcasecmp(value,"wrapper")==0))
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                      ": protocol name cannot be '",value,"'",NULL);

    /* Check if we already processed ApJServProperties directives */
    if (cfg->protocol!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Set up our value */
    cfg->protocol=jserv_protocol_getbyname(value);
    if (cfg->protocol==NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot find protocol '", value, "'", NULL);
    else return NULL;
}

/* ========================================================================= */
/* Handle ApJServDefaultHost directive (TAKE1) */
static const char *jserv_cfg_host(cmd_parms *cmd, void *dummy, char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    unsigned long address;

    /* Check if we already processed ApJServDefaultHost directives */
    if (cfg->host!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Copy our value in host */
    cfg->host=ap_pstrdup(cmd->pool,value);

    /* Resolve current host address */
    address=jserv_resolve(value);
    if (address==0)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot resolve host name '", value, "'", NULL);

    cfg->hostaddr=address;
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServDefaultPort directive (TAKE1) */
static const char *jserv_cfg_port(cmd_parms *cmd, void *dummy, char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check if we already processed ApJServManual directives */
    if (cfg->port!=JSERV_DEFAULT)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Set up our value */
    cfg->port=atoi(value);
    return NULL;
}

#ifdef LOAD_BALANCE

/* ========================================================================= */
/* Handle ApJHost directive (TAKE23) */
static const char *jserv_cfg_hosturl(cmd_parms *cmd, void *dummy,
                                   char *value1, char *value2, char *value3) {
    server_rec *s=cmd->server;
    pool *p=cmd->pool;
    jserv_config *cfg=jserv_server_config_get(s);
    jserv_host *hst = NULL;

    /* We check if mountpoint has a valid value */
    if (value1==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the first field (name) must be specified",
                          NULL);

    /* check if this named host already exists */
    if (cfg->hosturls!=NULL) {
      jserv_host *cur = cfg->hosturls;
      while (cur!=NULL) {
        if (cur->name && !strcmp(cur->name, value1)) {
          /* we found this host, use it */
          hst = cur;
          break;
        }
        cur=cur->next;
      }
    }
    
    /* if this host doesn't exist make a new one */
    if (hst == NULL) {
      hst=(jserv_host *) ap_pcalloc(p,sizeof(jserv_host));
      /* zero-out host id since this may get set later */
      hst->id = NULL;
    }

    /* Check if we have already some defined hosts. If we already have a host
       we insert current mount into the host list */
    if (cfg->hosturls!=NULL) {
        jserv_host *cur=cfg->hosturls;
        while (cur->next!=NULL) cur=cur->next;
        cur->next=hst;
    } else cfg->hosturls=hst;

    /* use the host identifier name */
    hst->name=value1;
    hst->config=cfg;

    /* Setup default (empty) values */
    hst->protocol=NULL;
    hst->host=NULL;
    hst->hostaddr=JSERV_DEFAULT;
    hst->port=JSERV_DEFAULT;
    hst->secretfile=NULL;
    hst->secret=NULL;
    hst->secretsize=JSERV_DEFAULT;
    hst->next=NULL;

    /* We check if our mounted uri has a valid value */
    if (value2!=NULL) {
        char *buf;
        char *tmp;
        char *protocol=NULL;
        char *host=NULL;
        char *port=NULL;
        char *zone=NULL;
        int x;

        /* We create a copy of value2 in temporary pool to keep original
           value safe */
        buf=ap_pstrdup(cmd->temp_pool,value2);
        tmp=buf;

        /* Try to find if we have a protocol://host */
        for (x=0; (x<128) && (buf[x]!='\0'); x++) {
            if ((buf[x]==':') && (buf[x+1]=='/') && (buf[x+2]=='/')) {
                if (x!=0) {
                    protocol=buf;
                    buf[x]='\0';
                }
                tmp=&buf[x+3];
            }
        }

        /* Check what was found after determining protocol */
        if (tmp[0]=='/') {
            zone=&tmp[1];
            host=NULL;
            tmp[0]='\0';
        } else if (tmp[0]==':') {
            port=&tmp[1];
            host=NULL;
            tmp[0]='\0';
        } else if (tmp[0]!='\0') host=tmp;
        tmp++;

        /* Check others (if we already got a zone we finished) */
        if (zone==NULL) {
            for (x=0; tmp[x]!='\0'; x++) {
                if (tmp[x]==':') {
                    port=&tmp[x+1];
                    tmp[x]='\0';
                } else if(tmp[x]=='/') {
                    zone=&tmp[x+1];
                    tmp[x]='\0';
                }
            }
        }

        /* Set protocol into structure */
        if (protocol!=NULL) {
            hst->protocol=jserv_protocol_getbyname(protocol);
            if (strcasecmp(protocol,"status")==0)
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): protocol name "
                                  "cannot be '",protocol,"'",NULL);
            if (hst->protocol==NULL) 
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): protocol '",
                                  protocol, "' cannot be found", NULL);

        }

        /* Set port into structure */
        if (port!=NULL) hst->port=atoi(port);

        /* Set host name and address into structure */
        if (host!=NULL) {
            unsigned long address;

            hst->host=ap_pstrdup(p,host);
            address=jserv_resolve(hst->host);
            
            if (address==0)
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): cannot resolve ",
                                  "host name '", host, "'", NULL);
            hst->hostaddr=address;
        }
    }

    /* Check if our secret file field is valid */
    if (value3!=NULL) {
        const char *ret;

        /* Get the secret key file contents and length */
        ret=jserv_readfile(cmd->pool, value3, JSERV_TRUE, &hst->secret,
                           &hst->secretsize);

        /* If ret is not null, an error occourred and ret points to message */
        if (ret!=NULL)
            return ap_pstrcat(cmd->pool, cmd->cmd->name,
                              ": secret file (3rd field): ", ret, NULL);
    }
    return NULL;
}

/* ========================================================================= */
/* Handle ApJRoute directive (TAKE2) */
static const char *jserv_cfg_route(cmd_parms *cmd, void *dummy, 
                                   char *value1, char *value2) {
    server_rec *s=cmd->server;
    pool *p=cmd->pool;
    jserv_config *cfg=jserv_server_config_get(s);
    jserv_host *hst = NULL;
    

    /* We check if host id has a valid value */
    if (value1==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the first field (identifier) must be specified",
                          NULL);

    /* We check if host id has a valid value */
    if (value2==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the second field (host name) must be specified",
                          NULL);
    /* check if this named host already exists */
    if (cfg->hosturls!=NULL) {
      jserv_host *cur = cfg->hosturls;
      while (cur!=NULL) {
      
        if (cur->name && !strcmp(cur->name, value2)) {
          /* we found this host, use it */
          hst = cur;
          break;
        }
        cur=cur->next;
      }
    }
    
    /* if this host doesn't exist make a new one and name it */
    if (hst == NULL) {
      hst=(jserv_host *) ap_pcalloc(p,sizeof(jserv_host));
      hst->name=value2;

      /* Check if we have already some defined hosts. If we already have a host
         we insert current mount into the host list */
      if (cfg->hosturls!=NULL) {
          jserv_host *cur=cfg->hosturls;
          while (cur->next!=NULL) cur=cur->next;
          cur->next=hst;
      } else cfg->hosturls=hst;
      }
    /* set the host identifier name */
    hst->id=value1;

    return NULL;
}


/* ========================================================================= */
/* Handle ApJServBalance directive (TAKE23) */
static const char *jserv_cfg_balance(cmd_parms *cmd, void *dummy, char *value1,
                                   char *value2, char *value3) {

    server_rec *s=cmd->server;
    pool *p=cmd->pool;
    jserv_config *cfg=jserv_server_config_get(s);
    jserv_balance *bal = (jserv_balance *) ap_pcalloc(p,sizeof(jserv_balance));

    /* We check if host id has a valid value */
    if (value1==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the first field (balance set name) must be specified",
                          NULL);

    /* We check if host id has a valid value */
    if (value2==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the second field (host id name) must be specified",
                          NULL);

    /* Check if we have already some defined balancers. If we already have some
       we insert current balance into the balancer list */
    if (cfg->balancers!=NULL) {
        jserv_balance *cur=cfg->balancers;
        while (cur->next!=NULL) cur=cur->next;
        cur->next=bal;
    } else cfg->balancers=bal;
    
    bal->name = value1;
    bal->host_name = value2;
    if (value3 != NULL) {
      bal->weight = atoi(value3);
    }
    else {
      bal->weight = 1;
    }
    
    return NULL;

}

#endif


/* ========================================================================= */
/* Handle ApJServMount directive (TAKE123) */
static const char *jserv_cfg_mount(cmd_parms *cmd, void *dummy, char *value1,
                                   char *value2, char *value3) {
    server_rec *s=cmd->server;
    pool *p=cmd->pool;
    jserv_config *cfg=jserv_server_config_get(s);
    jserv_mount *mnt=(jserv_mount *) ap_pcalloc(p,sizeof(jserv_mount));
    char *buf;
    char *tmp;
    int x,y;

    /* We check if mountpoint has a valid value */
    if (value1==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": the first field (directory) must be specified",
                          NULL);

    /* Check if we have already some defined mounts. If we already have a mount
       we insert current mount into the mount list */
    if (cfg->mount!=NULL) {
        jserv_mount *cur=cfg->mount;

        while (cur->next!=NULL) cur=cur->next;
        cur->next=mnt;
    } else cfg->mount=mnt;

    /* Setup default (empty) values */
    mnt->mountpoint=NULL;
    mnt->config=cfg;
    mnt->protocol=NULL;
    mnt->host=NULL;
    mnt->hostaddr=JSERV_DEFAULT;
    mnt->port=JSERV_DEFAULT;
    mnt->secretfile=NULL;
    mnt->secret=NULL;
    mnt->secretsize=JSERV_DEFAULT;
    mnt->zone=NULL;
#ifdef LOAD_BALANCE
    mnt->curr=NULL;
    mnt->hosturls=NULL;
    mnt->next=NULL;
#endif

    /* Remove double slash and add starting/trailing slash in mountpoint */
    /* NOTE: temporary buffer tmp is allocated under temporary pool */
    tmp=ap_pstrcat(cmd->temp_pool,"/",value1,NULL);
    buf=(char *)ap_pcalloc(cmd->temp_pool,strlen(tmp)+1);
    x=0; y=0; buf[y++]=tmp[x++];
    while(tmp[x]!='\0')
        if (tmp[x]=='/') (buf[y-1]!='/')?(buf[y++]=tmp[x++]):x++;
        else buf[y++]=tmp[x++];
    if (buf[y-1]!='/') (buf[y++]='/');
    buf[y]='\0';

    /* Map this mountpoint */
    mnt->mountpoint=ap_pstrdup(p,buf);

    /* We check if our mounted uri has a valid value */
    if (value2!=NULL) {
        char *buf;
        char *tmp;
        char *protocol=NULL;
        char *host=NULL;
        char *port=NULL;
        char *zone=NULL;
        int x;

        /* We create a copy of value2 in temporary pool to keep original
           value safe */
        buf=ap_pstrdup(cmd->temp_pool,value2);
        tmp=buf;

        /* Try to find if we have a protocol://host */
        for (x=0; (x<128) && (buf[x]!='\0'); x++) {
            if ((buf[x]==':') && (buf[x+1]=='/') && (buf[x+2]=='/')) {
                if (x!=0) {
                    protocol=buf;
                    buf[x]='\0';
                }
                tmp=&buf[x+3];
            }
        }

        /* Check what was found after determining protocol */
        if (tmp[0]=='/') {
            zone=&tmp[1];
            host=NULL;
            tmp[0]='\0';
        } else if (tmp[0]==':') {
            port=&tmp[1];
            host=NULL;
            tmp[0]='\0';
        } else if (tmp[0]!='\0') host=tmp;
        tmp++;

        /* Check others (if we already got a zone we finished) */
        if (zone==NULL) {
            for (x=0; tmp[x]!='\0'; x++) {
                if (tmp[x]==':') {
                    port=&tmp[x+1];
                    tmp[x]='\0';
                } else if(tmp[x]=='/') {
                    zone=&tmp[x+1];
                    tmp[x]='\0';
                }
            }
        }

        /* Set protocol into structure */
        if (protocol!=NULL) {
            mnt->protocol=jserv_protocol_getbyname(protocol);
            if (strcasecmp(protocol,"status")==0)
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): protocol name "
                                  "cannot be '",protocol,"'",NULL);
            if (mnt->protocol==NULL) 
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): protocol '",
                                  protocol, "' cannot be found", NULL);

        }

        /* Set zone into structure */
        if (zone!=NULL) mnt->zone=ap_pstrdup(p,zone);

        /* Set port into structure */
        if (port!=NULL) mnt->port=atoi(port);

        /* Set host name and address into structure */
        if (host!=NULL) {
            unsigned long address;

            mnt->host=ap_pstrdup(p,host);
            address=jserv_resolve(mnt->host);
            
#ifdef LOAD_BALANCE
        /* this is where we simply disabled the address checking */
        /* This was rather than checking protocol to see if it */
        /* is a virtual server since there are no other references */
        /* to protocol-specific code here. */
        /* Perhaps we can resolve the address in protocol-specific */
        /* code */
#else
            if (address==0)
                return ap_pstrcat(cmd->pool, cmd->cmd->name,
                                  ": mounted URL (2nd field): cannot resolve ",
                                  "host name '", host, "'", NULL);
#endif
            mnt->hostaddr=address;
        }
    }

    /* Check if our secret file field is valid */
    if (value3!=NULL) {
        const char *ret;

        /* Get the secret key file contents and length */
       mnt->secretfile=ap_pstrdup(p,value3);

        /* Get the secret key file contents and length */
        ret=jserv_readfile(cmd->pool, mnt->secretfile, JSERV_TRUE, 
                          &mnt->secret,
                           &mnt->secretsize);

        /* If ret is not null, an error occourred and ret points to message */
        if (ret!=NULL)
            return ap_pstrcat(cmd->pool, cmd->cmd->name,
                              ": secret file (3rd field): ", ret, NULL);
    }
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServMountCopy directive (FLAG) */
static const char *jserv_cfg_mountcopy(cmd_parms *cmd, void *dummy, int flag) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);

    /* Check if we already processed ApJServMountCopy directives */
    if (cfg->mountcopy!=JSERV_DEFAULT)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Set up our value */
    cfg->mountcopy=flag?JSERV_TRUE:JSERV_FALSE;
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServLogfile directive (TAKE1) */
static const char *jserv_cfg_logfile(cmd_parms *cmd, void *dummy, 
                                     char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    const char *ret;

    /* Check if we already processed ApJServLogFile directives */
    if (cfg->logfile!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    /* Open the log file */
    cfg->logfile=ap_pstrdup(cmd->pool,value);
    ret=jserv_openfile(cmd->pool, cfg->logfile, JSERV_TRUE,
                    &cfg->logfilefd, JSERV_LOGFILE_FLAGS, JSERV_LOGFILE_MODE);

    /* If ret is not null, an error occourred and ret points to message */
    if (ret!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name, ": ", ret, NULL);
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServLogLevel directive (TAKE1) */
static const char *jserv_cfg_loglevel(cmd_parms *cmd, void *dummy, 
                                     char *value) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    const char *ret, *str;

    ret = NULL;

    /* Check if we already processed ApJServLogLevel directives */
    if (cfg->loglevel!=JSERV_DEFAULT)
	ret = "cannot be specified more than once per host";
    /* code stolen from http_core.c */
    else if ((str = ap_getword_conf_nc(cmd->pool, &value))) {
        if (!strcasecmp(str, "emerg")) {
	    cfg->loglevel = APLOG_EMERG;
	}
	else if (!strcasecmp(str, "alert")) {
	    cfg->loglevel = APLOG_ALERT;
	}
	else if (!strcasecmp(str, "crit")) {
	    cfg->loglevel = APLOG_CRIT;
	}
	else if (!strcasecmp(str, "error")) {
	    cfg->loglevel = APLOG_ERR;
	}
	else if (!strcasecmp(str, "warn")) {
	    cfg->loglevel = APLOG_WARNING;
	}
	else if (!strcasecmp(str, "notice")) {
	    cfg->loglevel = APLOG_NOTICE;
	}
	else if (!strcasecmp(str, "info")) {
	    cfg->loglevel = APLOG_INFO;
	}
	else if (!strcasecmp(str, "debug")) {
	    cfg->loglevel = APLOG_DEBUG;
	}
	else {
            ret = "ApJServLogLevel requires level keyword: one of "
	           "emerg/alert/crit/error/warn/notice/info/debug";
	}
    }
    else {
        ret = "ApJServLogLevel requires level keyword";
    }

    /* If ret is not null, an error occourred and ret points to message */
    if (ret!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name, ": ", ret, NULL);
    return NULL;
}


/* ========================================================================= */
/* Handle ApJServSecretKey directive (TAKE1) */
static const char *jserv_cfg_secretkey(cmd_parms *cmd, void *dummy, 
                                       char *value) {
    jserv_config *cfg = jserv_server_config_get(cmd->server);
    const char *ret;

    /* Check if we already processed ApJServSecretKey directives */
    if (cfg->secretfile!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": cannot be specified more than once per host",
                          NULL);

    cfg->secretfile=ap_pstrdup(cmd->pool,value);

    /* Get the secret key file contents and length */
    ret=jserv_readfile(cmd->pool, cfg->secretfile, JSERV_TRUE, &cfg->secret,
                       &cfg->secretsize);

    /* If ret is not null, an error occourred and ret points to message */
    if (ret!=NULL)
        return ap_pstrcat(cmd->pool, cmd->cmd->name, ": ", ret, NULL);
    return NULL;
}

/* ========================================================================= */
/* Handle ApJServEnvVar directive (TAKE2) */
static const char *jserv_cfg_envvar(cmd_parms *cmd, void *dummy, 
                                       char *value1, char *value2) {
    jserv_config *cfg = jserv_server_config_get(cmd->server);
    const char *ret;

    ap_table_add(cfg->envvars, ap_pstrdup(cmd->pool,value1), ap_pstrdup(cmd->pool,value2));

    return NULL;
}

/* ========================================================================= */
/* Handle ApJServProtocolProperty directive (TAKE23) */
static const char *jserv_cfg_parameter(cmd_parms *cmd,void *dummy,char *value1,
                                       char *value2,char *value3) {
    jserv_config *cfg = jserv_server_config_get(cmd->server);
    jserv_protocol *proto = jserv_protocol_getbyname(value1);

    if (proto==NULL) 
        return ap_pstrcat(cmd->pool, cmd->cmd->name,
                          ": protocol '",
                          value1, "' cannot be found", NULL);
    /* Pass this parameters to the specified protocol and return */
    return jserv_protocol_parameter (proto, cfg, value2, value3);
}

/* ========================================================================= */
/* Handle ApJServAction directive (TAKE2) */
static const char *jserv_cfg_action(cmd_parms *cmd,void *dummy,char *value1,
                                       char *value2) {
    jserv_config *cfg = jserv_server_config_get(cmd->server);

    ap_table_setn(cfg->actions, value1, value2);
    return NULL;
}

/*****************************************************************************
 * Handle ApJServRetryAttempts directive (TAKE1)                             *
 *****************************************************************************/
static const char *jserv_cfg_setretry(cmd_parms *cmd, void *dummy, char *num) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    cfg->retryattempts = atoi(num);
    return NULL;
}

/*****************************************************************************
 * Handle ApJServVMTimeout directive (TAKE1)                             *
 *****************************************************************************/
static const char *jserv_cfg_setvmtimeout(cmd_parms *cmd, void *dummy,
		char *num) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    cfg->vmtimeout = atoi(num);
    if (cfg->vmtimeout < 3) cfg->vmtimeout = JSERV_DEFAULT_VMTIMEOUT;
    return NULL;
}

/*****************************************************************************
 * Handle ApJServVMInterval directive (TAKE1)                                *
 *****************************************************************************/
static const char *jserv_cfg_setvminterval(cmd_parms *cmd, void *dummy,
		char *num) {
    server_rec *s = cmd->server;
    jserv_config *cfg = jserv_server_config_get(s);
    cfg->vminterval = atoi(num);
    if (cfg->vminterval < 3) cfg->vminterval = JSERV_DEFAULT_VMINTERVAL;
    return NULL;
}

/*****************************************************************************
 * Apache Module procedures                                                  *
 *****************************************************************************/

/* ========================================================================= */
/* Clean up Apache JServ Module */
static void jserv_exit(void *data) {
    jserv_config *cfg=jserv_servers;
    int ret;

    /* Call our cleanup functions */
    ret=jserv_protocol_cleanupall(cfg, JSERV_FALSE);

    /* Log our exit if it was not clean */
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"Error cleaning-up protocols");
    }

    /* Log our init */
    jserv_error(JSERV_LOG_INFO,cfg,"Apache Module was cleaned-up");

    /* Our memory pool will be destroyed when the pool for which this cleanup
       was registered will be destroyed */
}

/* ========================================================================= */
/* Clean up Apache JServ when a child is exiting */
static void jserv_child_exit(void *data) {
    jserv_config *cfg=jserv_servers;
    int ret;

    /* Call our cleanup functions */
    ret=jserv_protocol_cleanupall(cfg, JSERV_TRUE);

    /* Log our exit if it was not clean */
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,
                    "Error cleaning-up protocols (ap_child)");
    }

    /* Log our init */
    jserv_error(JSERV_LOG_DEBUG,cfg,"Apache JServ Module was cleaned-up (ap_child)");
}

/* ========================================================================= */
/* Initialize Apache JServ Module */
static void jserv_init(server_rec *s, pool *p) {
    jserv_config *cfg=jserv_servers;
    int ret;

    /* Check ApJServSecretKey */
    if (cfg->secretfile==NULL) {
         printf("You must specify a secret key, or disable this feature.\nTo disable, add \"ApJServSecretKey DISABLED\" to your Apache configuration file.\nTo use, add \"ApJServSecretKey {filename}\" where filename is document\nwith more or less random contents, and perhaps a few kb in length.\nThe Apache JServ documentation explains this in more detail.\n");
         exit(1);
    }

    /* Setup base server defaults (in case merge_config was never called) */
    jserv_server_config_default(p, cfg);

    /* Log our init */
    jserv_error(JSERV_LOG_DEBUG,cfg,"Apache JServ Module is initializing");

#if MODULE_MAGIC_NUMBER >= 19980527
    /* Tell apache we're here */
    ap_add_version_component(JSERV_NAME "/" JSERV_VERSION);
#endif

    /* Create our memory pool */
    jserv_pool=ap_make_sub_pool(p);

    /* Init all protocols */
    ret=jserv_protocol_initall(cfg, JSERV_FALSE);

    /* Exit if we didn't initialize all protocols */
    if (ret==-1) {
        jserv_error_exit(JSERV_LOG_EMERG,cfg,"Error initializing protocols");
    }

    /* Register for clean exit */
    ap_register_cleanup(p, cfg, jserv_exit, ap_null_cleanup);
}

/* ========================================================================= */
/* Initialize Apache JServ Module when a new child is created */
static void jserv_child_init(server_rec *s, pool *p) {
    jserv_config *cfg=jserv_servers;
    int ret;

    /* Log our init */
    jserv_error(JSERV_LOG_DEBUG,cfg,"Apache JServ Module is initializing (ap_child)");

    /* Init all protocols */
    ret=jserv_protocol_initall(cfg, JSERV_TRUE);

    /* Exit if we didn't initialize all protocols */
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG, cfg, 
                    "Error initializing protocols (ap_child)");
    }

    /* Register for clean exit */
    ap_register_cleanup(p, cfg, ap_null_cleanup, jserv_child_exit);
}

/* ========================================================================= */
/* Match a request with a mount structure */
static jserv_request *jserv_translate_match(request_rec *r,jserv_mount *mount){
    char *mnt=mount->mountpoint;
    char *uri=r->uri;
    jserv_request *req=NULL;
    int x,y;

    /* If this is a proxy request, then decline to handle it. */
    if (r->proxyreq) {
              return NULL;
              }

    /* Matching URI and MNT discarding double slashes*/
    x=0; y=0;
    while ((uri[x]==mnt[y]) && (uri[x]!='\0')  && (mnt[y]!='\0')) {
        if (uri[x]=='/') while (uri[x+1]=='/') x++;
        x++;
        y++;
    }

    /* Check whether URI finished */
    if (uri[x]=='\0') {
        /* If MNT finished too or the remaining is only trailing slash we
           found a directory*/
        if ((mnt[y]=='\0') || ((mnt[y]=='/') && (mnt[y+1]=='\0'))) {
            req=(jserv_request *)ap_pcalloc(r->pool,sizeof(jserv_request));
            req->isdir=JSERV_TRUE;
            req->mount=mount;
            req->zone=mount->zone;
            req->servlet=NULL;
            return req;
        }

    /* If URI still contains data while MNT does not and the last character of
       URI parsed was a slash after it we find the target (zone/class) */
    } else if ((uri[x-1]=='/') && (mnt[y]=='\0')) {
        char *tmp=&uri[x];  /* Pointer to zone/class or class */
        int k=0;

        req=(jserv_request *)ap_pcalloc(r->pool,sizeof(jserv_request));
        req->isdir=JSERV_FALSE;
        req->mount=mount;
        req->zone=NULL;
        req->servlet=NULL;

        /* If zone is not specified in our mount structyre, check if we
           have a zone after the directory in URI */
        if (mount->zone==NULL) {
            while (tmp[k]!='\0') {
                if (tmp[k]=='/') {
                    req->zone=ap_pstrndup(r->pool,tmp,k);
                    /* Remove double slashes */
                    while (tmp[k]=='/') k++;
                    /* Check whether we have a null class name (dir request) */
                    if (tmp[k]!='\0') req->servlet=ap_pstrdup(r->pool,&tmp[k]);
                    else req->isdir=JSERV_TRUE;
                    return req;
                }
                k++;
            }

            /* If we didn't get a zone/class then we have a directory */
            req->zone=ap_pstrdup(r->pool,tmp);
            req->servlet=NULL;
            req->isdir=JSERV_TRUE;
            return req;
        }
        x = 0;
        while (tmp[x] != '/' && tmp[x] != 0)
            x++;
        if (tmp[x] == '/') {
            r->path_info = ap_pstrdup(r->pool,tmp+x);
            tmp[x] = 0;
        }
        req->zone=mount->zone;
        req->servlet=ap_pstrdup(r->pool,tmp);
        return req;
    }

    /* Otherwise what we parsed was not our business */
    return NULL;
}

/* ========================================================================= */
/* Match a request in current server config */
static int jserv_translate_handler(request_rec *r) {
    server_rec *s = r->server;
    jserv_config *cfg = jserv_server_config_get(s);
    jserv_mount *cur;
    jserv_request *result=NULL;

    /* If this is a proxy request, then decline to handle it. */
    if (r->proxyreq) {
              return DECLINED;
              }

    /* If we didn't get our server config we'll decline the request*/
    if (cfg==NULL) return DECLINED;

    /* If we didn't define any mounts we'll decline the request*/
    if (cfg->mount==NULL) return DECLINED;

    /* Is it an empty virtual server and is mountcopy off ?  */
    if ( (cfg->server != s) && (cfg->mountcopy!=JSERV_TRUE)) return DECLINED ;

    /* We are sure we have at least one mount point */
    cur=cfg->mount;

    /* Check all our structures for a possible match */
    while (cur!=NULL) {
        /* If our translator returns a non NULL pointer, we got it */
        if ((result=jserv_translate_match(r, cur))!=NULL) {
            /* Block direct requests to Apache JServ as servlet */
            if (strstr(r->uri,"/" JSERV_SERVLET)!=NULL) {
                return FORBIDDEN;
            }
            ap_set_module_config(r->request_config, &jserv_module,result);
            r->handler=ap_pstrdup(r->pool,"jserv-servlet");
            return OK;
        }
        cur=cur->next;
    }

    /* If this was not matched we decline */
    return DECLINED;
}

/* ========================================================================= */
/* Match a request in current server config (from mod_mime and mod_actions) */
static int jserv_type_match(request_rec *r) {
    server_rec *s = r->server;
    jserv_config *cfg = jserv_server_config_get(s);
    const char *servlet=NULL;
    char *file=NULL;
    char *ext=NULL;

    /* If this is a proxy request, then decline to handle it. */
    if (r->proxyreq) {
              return DECLINED;
              }
    /* Check filename */
    if (r->filename==NULL) return DECLINED;
    file=strrchr(r->filename, '/');
    if (file==NULL) file=r->filename;
    ext=strrchr(file, '.');
    if (ext==NULL) return DECLINED;

    /* Get and check extension */
    servlet=ap_table_get(cfg->actions, ext);
    if (servlet==NULL) return DECLINED;

    /* Set the servlet name in r->notes table */
    ap_table_set(r->notes,"jserv-action",servlet);
    r->handler=ap_pstrdup(r->pool,"jserv-action");

    return OK;
}

/* ========================================================================= */
/* Handle an action request (redirect from extension to servlet) */
static int jserv_handler_action(request_rec *r) {
    server_rec *s = r->server;
    jserv_config *cfg = jserv_server_config_get(s);
    const char *servlet=NULL;
    char *uri=NULL;

    /* Check the servlet name passed by jserv_type_match */
    servlet=ap_table_get(r->notes, "jserv-action");
    if (servlet==NULL) {
        jserv_error(JSERV_LOG_INFO,cfg,"Action with no servlet name received",
                    r->filename, r->path_info);
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    /* Format servlet uri and redirect */
    uri=ap_pstrcat(r->pool, servlet, r->args?"?":NULL, r->args, NULL);
    ap_internal_redirect_handler(uri,r);
    
    /* tell Apache we handled this request */
    return OK;
}

/* ========================================================================= */
/* Handle a servlet request */
static int jserv_handler_servlet(request_rec *r) {
    const char *old_if_modified_since = NULL;
    server_rec *s = r->server;
    jserv_config *cfg = jserv_server_config_get(s);
    jserv_request *req=ap_get_module_config(r->request_config, &jserv_module);
    jserv_protocol *proto=NULL;
    int ret;

    if( req == NULL) {
	jserv_mount * mnt;
	/* 
	   This is called if SetHandler was used - 
	   in such case, no request config is available, and we create one
	*/
	req = ap_pcalloc(r->pool, sizeof(jserv_request));
	req->zone = NULL;
	req->servlet = NULL;
	req->isdir=0;
	req->mount=cfg->mount;
	for( mnt=cfg->mount; mnt != NULL; mnt = mnt->next) {
	    if( (!strcmp(mnt->mountpoint,"*")) || (!strcmp(mnt->mountpoint,"default"))) {
		req->mount = mnt;
		break;
	    }
	}
    }



    /* If this is a proxy request, then decline to handle it. */
    if (r->proxyreq) {
        return DECLINED;
    }
    /* if no_local_copy is set, we should ignore if-modified-since, as it *
     * would not be valid to tell the client to use its local copy.       *
     * it might be nice to use ap_meets_conditions here ... but we don't  *
     * really do ETag stuff yet anyway.                                   */
    if (r->no_local_copy) {
        old_if_modified_since = ap_table_get(r->headers_in,"if-modified-since");
        ap_table_unset(r->headers_in, "if-modified-since");
    }

    /* If this was an internal redirection from Apache JServ then our
     * path_info is previous uri */
    if (r->prev!=NULL) {
        if (r->prev->handler!=NULL) {
            if (strcasecmp(r->prev->handler,"jserv-action")==0) {
                r->path_info=r->prev->uri;
                r->filename=NULL;
            }
        }
        if (r->prev->status==404) {
            r->path_info=r->prev->uri;
        }
    }

    /* Check if we have a per request or per server protocol and use it */
    if (req->mount->protocol!=NULL) proto=req->mount->protocol;
    else if (cfg->protocol!=NULL) proto=cfg->protocol;
    else {
        /* Why we did not find a protocol? */
        jserv_error(JSERV_LOG_EMERG,cfg,
                    "cannot find a protocol for request %s on host %s",
                    r->uri, r->hostname);
        return HTTP_INTERNAL_SERVER_ERROR;
    }
    
    /* Handle request to protocol */
    ret=jserv_protocol_handler (proto,cfg,req,r);

    /* the r->status is used internally by mod_jserv to distinguish between  */
    /* error sent by servlets and errors coming from a non-responding JServ. */
    /* This corrects a Denial of Service bug in load-balancing, but whe have */
    /* to clean it before returning it to Apache, to prevent funny results : */
    /*"Additionally , a 404 Not Found error was encountered while trying to  */
    /* use an ErrorDocument to handle the request." by example.              */
     r->status = HTTP_OK;

    if (old_if_modified_since != NULL) 
		ap_table_set(r->headers_in, "If-Modified-Since", old_if_modified_since);

    /* Apache will understand the protocol handler return codes (either
     * OK == 0 or HTTP_INTERNAL_SERVER_ERROR == 500) so this is correct.
     */
    return ret;
}

/* ========================================================================= */
/* Handle a status request */
static int jserv_handler_status(request_rec *r) {
    server_rec *s = r->server;
    jserv_config *cfg = jserv_server_config_get(s);
    jserv_protocol *proto = jserv_protocol_getbyname("status");
    jserv_request *req = ap_pcalloc(r->pool, sizeof(jserv_request));
    int ret;

    if (proto==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,
                    "cannot find protocol 'status' for status handler");
        return HTTP_INTERNAL_SERVER_ERROR;
    }
    
    ret=jserv_protocol_handler (proto,cfg,req,r);
    return ret;
}


/*****************************************************************************
 * Predefined Apache and Apache JServ stuff                                  *
 *****************************************************************************/

/* ========================================================================= */
/* List of Apache JServ handlers */
static handler_rec jserv_handlers[] = {
    {"jserv-servlet", jserv_handler_servlet},
    {"jserv-status", jserv_handler_status},
    {"jserv-action", jserv_handler_action},
    {NULL}
};

/* ========================================================================= */
/* List of directives for Apache */
static command_rec jserv_commands[] = {
    {"ApJServManual", jserv_cfg_manual, NULL, RSRC_CONF, FLAG,
     "Whether Apache JServ is running in manual or automatic mode."},
    {"ApJServProperties", jserv_cfg_properties, NULL, RSRC_CONF, TAKE1,
     "The full pathname of jserv.properties file."},
    {"ApJServDefaultProtocol", jserv_cfg_protocol, NULL, RSRC_CONF, TAKE1,
     "The default protocol used for connecting to Apache JServ."},
    {"ApJServDefaultHost", jserv_cfg_host, NULL, RSRC_CONF, TAKE1,
     "The default host running Apache JServ."},
    {"ApJServDefaultPort", jserv_cfg_port, NULL, RSRC_CONF, TAKE1,
     "The default port on which Apache JServ is running on."},
    {"ApJServMount", jserv_cfg_mount, NULL, RSRC_CONF, TAKE123,
     "Where Apache JServ servlets will be mounted under Apache."},
    {"ApJServMountCopy", jserv_cfg_mountcopy, NULL, RSRC_CONF, FLAG,
     "Whether <VirtualHost> inherits base host mount points or not."},
    {"ApJServLogFile", jserv_cfg_logfile, NULL, RSRC_CONF, TAKE1,
     "Apache JServ log file relative to Apache root directory."},
    {"ApJServLogLevel", jserv_cfg_loglevel, NULL, RSRC_CONF, TAKE1,
     "Apache JServ log verbosity."},
    {"ApJServSecretKey", jserv_cfg_secretkey, NULL, RSRC_CONF, TAKE1,
     "Apache JServ secret key file relative to Apache root directory."},
    {"ApJServProtocolParameter", jserv_cfg_parameter, NULL, RSRC_CONF, TAKE23,
     "Apache JServ protocol-dependant property."},
    {"ApJServAction", jserv_cfg_action, NULL, RSRC_CONF, TAKE2,
     "Apache JServ action mapping extension to servlets."},
#ifdef LOAD_BALANCE
    {"ApJServBalance", jserv_cfg_balance, NULL, RSRC_CONF, TAKE23,
     "Apache JServ load-balancing server set."},
    {"ApJServHost", jserv_cfg_hosturl, NULL, RSRC_CONF, TAKE23,
     "Apache JServ host definition."},
    {"ApJServRoute", jserv_cfg_route, NULL, RSRC_CONF, TAKE2,
     "Apache JServ host routing identifier."},
    {"ApJServShmFile", jserv_cfg_shmfile, NULL, RSRC_CONF, TAKE1,
     "The full pathname of shared memory file."},
#endif
    {"ApJServRetryAttempts", jserv_cfg_setretry, NULL, RSRC_CONF, TAKE1,
     "Apache JServ: retry attempts (1s appart) before returning server error"},
    {"ApJServVMTimeout", jserv_cfg_setvmtimeout, NULL, RSRC_CONF, TAKE1,
     "Apache JServ: the amount of time given for the JVM to start or stop"},
    {"ApJServVMInterval", jserv_cfg_setvminterval, NULL, RSRC_CONF, TAKE1,
     "Apache JServ: the interval between 2 polls of the JVM "},
    {"ApJServEnvVar", jserv_cfg_envvar, NULL, RSRC_CONF, TAKE2,
     "Apache JServ: protocol ajpv12 : env var to send to the server"},
    {NULL}
};

/* ========================================================================= */
/* Our mod_jserv module structure */
module MODULE_VAR_EXPORT jserv_module = {
    STANDARD_MODULE_STUFF,
    jserv_init,                 /* module initializer */
    NULL,                       /* per-directory config creator */
    NULL,                       /* dir config merger */
    jserv_server_config_create, /* server config creator */
    jserv_server_config_merge,  /* server config merger */
    jserv_commands,             /* command table */
    jserv_handlers,             /* [7] list of handlers */
    jserv_translate_handler,    /* [2] filename-to-URI translation */
    NULL,                       /* [5] check/validate user_id */
    NULL,                       /* [6] check user_id is valid *here* */
    NULL,                       /* [4] check access by host address */
    jserv_type_match,           /* [7] MIME type checker/setter */
    NULL,                       /* [8] fixups */
    NULL,                       /* [10] logger */
    NULL,                       /* [3] header parser */
#if MODULE_MAGIC_NUMBER > 19970622
    jserv_child_init,           /* apache child process initializer */
    NULL,                       /* apache child process exit/cleanup */
    NULL                        /* [1] post read_request handling */
#endif
};

