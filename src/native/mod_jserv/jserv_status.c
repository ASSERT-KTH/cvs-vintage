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
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Based on:    mod_jserv.c by Alexei Kosut <akosut@apache.org>              *
 *              mod_example.c by Apache Group <apache@apache.org>            *
 * Modified by: Pierpaolo Fumagalli <ianosh@iname.com> June, 12 1998         *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/
#include "jserv.h"

/*****************************************************************************
 * Local procedures for jserv_status protocol                                *
 *****************************************************************************/

/* ========================================================================= */
/* Remote configured hosts (JVMs) */
typedef struct jserv_status_host jserv_status_host;

struct jserv_status_host {
    char *url;
    jserv_mount *mount;
    jserv_status_host *next;
};

/* ========================================================================= */
/* Remote configured hosts (JVMs) */
jserv_status_host *jserv_status_hosts=NULL;
pool *jserv_status_pool=NULL;

/* ========================================================================= */
/* Local output functions */
static int jserv_status_out_menu(jserv_config *cfg, jserv_request *req,
                                  request_rec *r);
static int jserv_status_out_image(jserv_config *cfg, jserv_request *req,
                                   request_rec *r);
static int jserv_status_out_server(jserv_config *cfg, jserv_request *req,
                                   request_rec *r);
static int jserv_status_out_jserv(jserv_config *cfg, jserv_request *req,
                                   request_rec *r, int mount_number);
static int jserv_status_out_baljserv(jserv_config *cfg, jserv_request *req,
                          request_rec *r);
static int jserv_status_out_baljserv2(jserv_config *cfg, jserv_request *req,
                          request_rec *r, jserv_mount *mnt, jserv_host *cur);

static void jserv_change_status(jserv_config *cfg, jserv_host *cur, char sta);

/*****************************************************************************
 * Code for jserv_status protocol                                            *
 *****************************************************************************/

/* ========================================================================= */
/* Our request handler */
static int jserv_status_handler(jserv_config *cfg,jserv_request *req,
                                request_rec *r) {
    int ret;
    char *tmp;

    ap_hard_timeout("status-handler", r);

    if ((tmp=strstr(r->uri,"/engine/"))!=NULL) {
        int mount_number=0;
         /* Check if we had module=(null) or less... no server*/
         if (*tmp == '\0') { /* invalid URL -- must have an integer there */
             ret = FORBIDDEN;
         } else {
             if ((strstr(tmp, "/direct/"))!=NULL) {
                 /* quick fix. the Servlet should return absolute paths instead */
                 if (r->args && strcasecmp(r->args,"image")==0)
                     ret=jserv_status_out_image(cfg,req,r); 
                 else 
                     ret=jserv_status_out_baljserv(cfg,req,r); 
                 
             } else {
                 mount_number=atoi(tmp+8); /* well, it might not be an int, but */
                 ret=jserv_status_out_jserv(cfg,req,r,mount_number); /* if not */
             }                         /* mount_number will be 0, which is. */
         }
    /* If we had no query then we want the MENU */
    } else if (r->args==NULL) {
        ret=jserv_status_out_menu(cfg,req,r);
    /* If we had query="menu" then we want the MENU */
    } else if (strcasecmp(r->args,"menu")==0) {
        ret=jserv_status_out_menu(cfg,req,r);
    /* If we had query="image" then we want the IMAGE */
    } else if (strcasecmp(r->args,"image")==0) {
        ret=jserv_status_out_image(cfg,req,r);
    /* If we had query="module" then we want the SERVER STATUS */
    } else if (strcasecmp(r->args,"module")>0) {
        /* Check if we had module=(null) or less... no server*/
        if (strlen(r->args)<=7) ret=FORBIDDEN;
        else {
        /* If we have module=server_name */
            char *buf=&r->args[7];
            jserv_config *cur=jserv_servers;

            /* Set default return 500 (internal server error) */
            ret=SERVER_ERROR;
            /* Iterate thru all configurations to match server name */
            while (cur!=NULL) {
                if (cur->server!=NULL) {
                    if (strcmp(cur->server->server_hostname,buf)==0) {
                        /* Return what server returned */
                        ret=jserv_status_out_server(cur,req,r);
                        break;
                    }
                }
                cur=cur->next;
            }
        }
    } else ret=FORBIDDEN;
    
    ap_kill_timeout(r);
    return ret;
}

/* ========================================================================= */
/* Check and add if not found (except zone) a mountpoint to hosts */
static void jserv_status_add_host(pool *p, jserv_mount *mount) {
    jserv_status_host *cur=jserv_status_hosts;
    jserv_status_host *new=NULL;

    /* Iterate thru all mount points */
    while (cur!=NULL) {
        /* Check if we found the same mount point */
        if (cur->mount==mount) return;

        /* Check protocol,host and port */
        if ((cur->mount->protocol==mount->protocol) &&
            (cur->mount->hostaddr==mount->hostaddr) &&
            (!strcmp(cur->mount->host,mount->host)) &&
            (cur->mount->port==mount->port)) return;

        cur=cur->next;
    }

    /* Add to the list */
    new=(jserv_status_host *)ap_pcalloc(p,sizeof(jserv_status_host));
    new->url=ap_psprintf(p, "%s://%s:%d", mount->protocol->name, mount->host,
                         mount->port);
    new->mount=mount;
    new->next=jserv_status_hosts;
    jserv_status_hosts=new;
    return;
}

/* ========================================================================= */
/* Our protocol initializer */
static int jserv_status_init(jserv_config *cfg) {
    jserv_config *cur=jserv_servers;
    jserv_status_pool=ap_make_sub_pool(jserv_pool);
    jserv_status_hosts = NULL;

    while (cur!=NULL) {
        jserv_mount *rcur=cur->mount;

        while (rcur!=NULL) {
            jserv_status_add_host(jserv_status_pool, rcur);
            rcur=rcur->next;
        }        
        cur=cur->next;
    }
    return 0;
}

/*****************************************************************************
 * Local output functions                                                    *
 *****************************************************************************/

/* ========================================================================= */
/* Output a table (<td>...</td>) */
static void jserv_status_out_table(request_rec *r, const char *bkg,
                                   const char *param, const char *fmt, ...) {
    va_list ap;
    char *buf;

    va_start(ap,fmt);
    buf=ap_pvsprintf(r->pool,fmt,ap);
    va_end(ap);

    ap_rputs("    <td", r);
    if (bkg!=NULL) ap_rprintf(r," bgcolor=\"%s\"",bkg);
    if (param!=NULL) ap_rprintf(r," %s",param);
    ap_rputs(">\n",r);
    ap_rprintf(r,"      %s\n",buf);
    ap_rputs("    </td>\n",r);
}

/* ========================================================================= */
/* Output the HEAD (head, title, banner ...) */
static void jserv_status_out_head(request_rec *r) {
    ap_rputs("<html>\n",r);
    ap_rputs("<head>\n",r);
    ap_rputs("<meta name=\"GENERATOR\" CONTENT=\"" JSERV_NAME " " JSERV_VERSION "\">\n",r);
    ap_rputs("<title>" JSERV_NAME " " JSERV_VERSION " Status</title>\n",r);
    ap_rputs("</head>\n",r);
    ap_rputs("<body bgcolor=\"#ffffff\" text=\"#000000\">\n",r);
    ap_rputs("<p align=center><img src=\"/jserv/status?image\"></p>\n",r);
}

/* ========================================================================= */
/* Output the TAIL (copyright, link...) */
static void jserv_status_out_tail(request_rec *r) {
    ap_reset_timeout(r);
    ap_rputs("<p align=\"center\"><font size=-1>\n",r);
    ap_rputs("Copyright (c) 1997-99 <a href=\"http://java.apache.org\">",r);
    ap_rputs("The Java Apache Project</a>.<br>\n",r);
    ap_rputs("All rights reserved.\n",r);
    ap_rputs("</font></p>\n",r);
    ap_rputs("</body>\n",r);
    ap_rputs("</html>\n",r);    
}

/* ========================================================================= */
/* Output the MENU */
static int jserv_status_out_menu(jserv_config *cfg, jserv_request *req,
                                  request_rec *r) {
    jserv_config *cur=jserv_servers;
    jserv_status_host *rcur=jserv_status_hosts;
    int count = 0;

    /* Set type and evaluate if this is a handler-only request */
    r->content_type = "text/html";
    ap_send_http_header(r);
    if (r->header_only) return OK;

    /* Real output */
    jserv_status_out_head(r);
    
    ap_rputs("<center><table border=\"0\" width=\"60%\" bgcolor=\"#000000\" cellspacing=\"0\" cellpadding=\"0\">\n",r);
    ap_rputs(" <tr>\n",r);
    ap_rputs("  <td width=\"100%\">\n",r);
    ap_rputs("   <table border=\"0\" width=\"100%\" cellpadding=\"4\">\n",r);
    ap_rputs("    <tr>\n",r);
    ap_rputs("     <td width=\"100%\" bgcolor=\"#c0c0c0\"><p align=\"right\"><big><big>" JSERV_NAME " " JSERV_VERSION " Status</big></big></td>\n",r);
    ap_rputs("    </tr><tr>\n",r);
    ap_rputs("     <td width=\"100%\" bgcolor=\"#e0e0e0\">Welcome to the dynamic status page of the " JSERV_NAME "\n",r);
    ap_rputs("     servlet engine. All these pages are dynamically created to show you the status of your\n",r);
    ap_rputs("     servlet execution environment, both on the web server side (generated by mod_jserv) and on\n",r);
    ap_rputs("     the servlet engine side (yes, " JSERV_NAME " is actually a servlet and executes itself!)</td>\n",r);
    ap_rputs("    </tr>\n",r);
    ap_rputs("    <tr>\n",r);
    ap_rputs("     <td width=\"100%\" bgcolor=\"#f0f0f0\"><br><br>\n",r);
    ap_rputs("      <center>\n",r);
    ap_rputs("       <table border=\"0\" bgcolor=\"#000000\" cellspacing=\"0\" cellpadding=\"0\">\n",r);
    ap_rputs("        <tr>\n",r);
    ap_rputs("         <td width=\"100%\">\n",r);
    ap_rputs("          <table border=\"0\" width=\"100%\" cellpadding=\"4\">\n",r);
    ap_rputs("           <tr>\n",r);
    ap_rputs("           <td bgcolor=\"#c0c0c0\" width=\"50%\" valign=\"middle\" align=\"center\" nowrap><b>Configured hosts</b></td>",r);
    ap_rputs("           <td bgcolor=\"#c0c0c0\" width=\"50%\" valign=\"middle\" align=\"center\" nowrap><b>Mapped servlet engines</b></td>",r);
    ap_rputs("           </tr>\n",r);
    ap_rputs("           <tr>\n",r);
    ap_rputs("            <td bgcolor=\"#ffffff\" valign=\"middle\" align=\"center\" nowrap>",r);
                  
    /* Examine list of Apache virtualhosts currently configured for Apache JServ */
    ap_reset_timeout(r);
    while (cur!=NULL) {
        char *name=cur->server->server_hostname;
        ap_rprintf(r,"<a href=\"./status?module=%s\">%s</a>",name,name);
        if (cur==cfg) ap_rputs(" <font size=-1><i>(current)</i></font>",r);
        ap_rputs("<br>\n",r);
        cur=cur->next;
    }
    
    ap_rputs("            </td>\n",r);
    ap_rputs("            <td bgcolor=\"#ffffff\" valign=\"middle\" align=\"center\" nowrap>",r);
    
    /* List of JServ hosts */
    while (rcur!=NULL) {
        ap_rprintf(r,"<a href=\"./engine/%d/\">%s</a>", count++, rcur->url);
        ap_rputs("<br>\n",r);
        rcur=rcur->next;
    }

    ap_rputs("            </td>\n",r);
    ap_rputs("           </tr>\n",r);
    ap_rputs("          </table>\n",r);
    ap_rputs("         </td>\n",r);
    ap_rputs("        </tr>\n",r);
    ap_rputs("       </table>\n",r);
    ap_rputs("      </center><br>\n",r);
    ap_rputs("     </td>\n",r);
    ap_rputs("    </tr>\n",r);
    ap_rputs("    <tr>\n",r);
    ap_rputs("     <td width=\"100%\" bgcolor=\"#FFFFFF\"><strong>Warning</strong>: you should restrict access to\n",r);
    ap_rputs("     this page on a production environment since it may give untrusted users access to\n",r);
    ap_rputs("     information you may want to remain secret.</td>\n",r);
    ap_rputs("    </tr>\n",r);
    ap_rputs("   </table>\n",r);
    ap_rputs("  </td>\n",r);
    ap_rputs(" </tr>\n",r);
    ap_rputs("</table><center>\n",r);
    jserv_status_out_tail(r);
    
    return OK;
}

char * get_html(char *dest, char *hst, char *id, char state) {
char *ptr;
char *str = "\
<FORM ACTION=\"/jserv/engine/direct/%s/%s/\" METHOD=GET>\
<FONT FACE=\"Arial,Helvetica\" SIZE=\"-2\">\
%s's current shm state: <br>\
<SELECT NAME=\"current\">\
<OPTION VALUE=\"\">%s \
</SELECT>\
<INPUT TYPE=\"submit\" name=\"testbutton\" value=\"test\">\
<br>change to:<br>\
<SELECT NAME=\"newst\">\
<OPTION VALUE=\"\">choose\
<OPTION VALUE=\"+\">Up (+)\
<OPTION VALUE=\"-\">Down (-) \
<OPTION VALUE=\"/\">Stop (/) \
<OPTION VALUE=\"X\">Stop (X) \
</SELECT>\
<INPUT TYPE=\"submit\" name=\"changebutton\" value=\"apply\">\
</FONT>\
</FORM>\
";
switch (state) {
  case '+': ptr="Up (+)";break;
  case '-': ptr="Down (-)";break;
  case '/': ptr="Stopped gently (/)";break;
  case 'X': ptr="Stopped (X)";break;
  default: ptr="try it";break;
};
sprintf(dest, str, hst, id, id, ptr, hst, id);
return dest;
}
/* ========================================================================= */
/* Output the Server Configuration */
static int jserv_status_out_server(jserv_config *cfg, jserv_request *req,
                                   request_rec *r) {
    jserv_mount *cur=NULL;
    jserv_host *first;

    /* This is so we can use inet_ntoa, which is used for reporting
     * a network address in human-readable format. */
    struct in_addr network_address;
    network_address.s_addr = cfg->hostaddr;

    /* Set type and evaluate if this is a handler-only request */
    r->content_type = "text/html";
    ap_send_http_header(r);
    if (r->header_only) return OK;

    /* Real output */
    jserv_status_out_head(r);

    /* Output table headers */
    ap_rputs("<center>\n",r);
    ap_rputs("<table width=\"60%\" border=0>\n",r);
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#c0c0c0","width=50% valign=top align=center",
                           "<b>Parameter</b>");
    jserv_status_out_table(r,"#c0c0c0","width=50% valign=top align=center",
                           "<b>Value</b>");
    ap_rputs("  </tr>\n",r);

    /* Output Server Name */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "<b>Server Name</b>");
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s",cfg->server->server_hostname);
    ap_rputs("  </tr>\n",r);


    /* Output Manual Mode */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "<b>ApJServManual</b>");
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                    "%s",(cfg->manual==JSERV_TRUE)?
                    "TRUE <font size=-2>(STANDALONE OPERATION)</font>":
                    "FALSE <font size=-2>(AUTOMATIC OPERATION)</font>");

    /* Output Properties File */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "<b>ApJServProperties</b>");

    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s <font size=-2>%s</font>",cfg->properties,
                           (cfg->manual==JSERV_TRUE)?
                           "(IGNORED)":
                           "");

    /* Output Protocol */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "<b>ApJServDefaultProtocol</b>");
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "%s <font size=-2>(PORT %d)</font>",
                           cfg->protocol->name,cfg->protocol->port);

    /* Output Host */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "<b>ApJServDefaultHost</b>");
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s <font size=-2>(ADDR %s)</font>",
                           cfg->host,
                           inet_ntoa(network_address));

    /* Output Port */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "<b>ApJServDefaultPort</b>");
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "%d",cfg->port);

    /* Output Logfile */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "<b>ApJServLogFile</b>");
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s <font size=-2>(DESCRIPTOR %d)</font>",
                           cfg->logfile, cfg->logfilefd);

    /* Output Mountcopy */
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "<b>ApJServMountCopy</b>");
    jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                           "%s",(cfg->mountcopy==JSERV_TRUE)?
                                       "TRUE":"FALSE");
    /* shared memory file  */
#ifdef LOAD_BALANCE
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "<b>ApJServShmFile</b>");
    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s",
                           cfg->shmfile?cfg->shmfile:"undefined");
#endif


    /* Properties done */
    ap_rputs("  </tr>\n",r);
    ap_rputs("</table>\n",r);

    /* Output table headers for mounts */
    ap_rputs("<br>\n",r);
    ap_rputs("<table width=\"60%\" border=0>\n",r);
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>MountPoint</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Server</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Protocol</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Host</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Port</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Zone</b>");
    jserv_status_out_table(r,"#b0b0b0","valign=top align=center",
                           "<b>Status</b>");
    ap_rputs("  </tr>\n",r);

    /* Check for valid mountpoints */
    cur=cfg->mount;
    ap_reset_timeout(r);
    while (cur!=NULL) {
      int notbalanced; /* 0  in a balanced mountpoint, 1 otherwise */
      int stilltrue;   /* used to print all hosts for a mountpoint */
      int isheader;    /* 1 we are printing the header, 0 otherwise*/
      jserv_host *target;
#ifdef LOAD_BALANCE
      char hoststatus;
      if (!strcmp(cur->protocol->name, "balance")) {
            notbalanced = 0;
            target = cur->hosturls;
           /* every httpd has its own (if protocol=balance) entry point in */
           /* the circular list. (this entry point is set the functiion    */
           /* jserv_balance.c:choose_default_target().                     */
           /* we try to start always from the same one for presenting the  */
           /* results.                                                     */
           {
               jserv_host *tmph = target;
               first= target;
               while (tmph) {
                   if (strcmp(tmph->id, target->id)) {
                           target = tmph;
                           break;         /* target= 1st occ. of any  host */
                   }
                   tmph = tmph->next;
                   if (tmph == first)
                       break;
               }
               first= target;
               tmph = target;
               while (tmph) {
                   if (strcmp(tmph->id, target->id) < 0) {
                           target = tmph;
                   }
                   tmph = tmph->next;
                   if (tmph == first)
                       break;
               }                  /* target is the 1rst of the lowest ids */
           }
        }
        else
#endif
            notbalanced = 1;
        isheader = 1;
        stilltrue = 1;
           
        first= target;
        
        network_address.s_addr = cur->hostaddr;
        while (stilltrue) {
            if (notbalanced)
               stilltrue = 0;

            ap_rputs("  <tr>\n",r);
            jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               "%s",isheader?
                                           cur->mountpoint:
                                           "");


            if (isheader) {
                isheader=0;
                jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s", cur->config->server->server_hostname);
                jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                           "%s", cur->protocol?
                           cur->protocol->name:
                           "DEFAULT");
                if (notbalanced) {
                    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               "%s <font size=-2>(ADDR %s)</font>",
                               cur->host?cur->host:"DEFAULT",
                               inet_ntoa(network_address));

                    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               "%d",cur->port);
                }
                else {
                    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               "<b>%s</b>",
                               cur->host?cur->host:"????");

                    jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               " ");
                }
                jserv_status_out_table(r,"#f0f0f0","valign=top align=left",
                               "%s", cur->zone?cur->zone:"URI MAPPED");
                ap_rputs("  </tr>\n",r);
                ap_reset_timeout(r);
            }
#ifdef LOAD_BALANCE
            else {
                int weight=0;
                char bigbuffer[2048];
                network_address.s_addr = target->hostaddr;
               /* if weigth > 1, just show the target once without displaying the whole ring */ 
               {
                 jserv_host *first= target;
                 jserv_host *tmph = target;
                 while (tmph) {
                   if (!strcmp(tmph->id, target->id)) {
                      target = tmph;
                      weight++;
                   }
                   tmph = tmph->next;
                   if (tmph == first)
                       break;
                 }
               }
                jserv_status_out_table(r,"#f0f0f0","valign=center align=center",
                           "%s<br>weight=%d", target->name, weight);
                jserv_status_out_table(r,"#e0e0e0","valign=center align=center",
                               "%s", target->protocol?
                               target->protocol->name:
                               "DEFAULT");
                jserv_status_out_table(r,"#e0e0e0","valign=center align=center",
                               "%s <font size=-2>(ADDR %s)</font>",
                               target->host?target->host:"DEFAULT",
                               inet_ntoa(network_address));

                jserv_status_out_table(r,"#e0e0e0","valign=center align=center",
                               "%d",target->port);
                jserv_status_out_table(r,"#e0e0e0","valign=center align=center", "\"");
                /* access the share memory to get the last known status */
                hoststatus = jserv_getstate(cfg, target);
                if (!hoststatus)
                    hoststatus='?';
                jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                        "%s",
                        get_html(bigbuffer, cfg->server->server_hostname, target->id, hoststatus));

                ap_rputs("  </tr>\n",r);
                ap_reset_timeout(r);
                target = target->next;
                if(target==0 || target==first)
                    stilltrue=0;
            }                        
        }
#endif
        cur=cur->next;
    }

    /* Mounts done */
    ap_rputs("  </tr>\n",r);
    ap_rputs("</table>\n",r);
    
    /* See ACTIONS commands */
    ap_reset_timeout(r);
    ap_rputs("<br>\n",r);
    ap_rputs("<table width=\"60%\" border=0>\n",r);
    ap_rputs("  <tr>\n",r);
    jserv_status_out_table(r,"#c0c0c0","valign=top align=center",
                           "<b>Extension</b>");
    jserv_status_out_table(r,"#c0c0c0","valign=top align=center",
                           "<b>Servlet</b>");

    if (!ap_is_empty_table(cfg->actions)) {
        array_header *actions=ap_table_elts(cfg->actions);
        table_entry *elts=(table_entry *) actions->elts;
        int i;

        for (i = 0; i < actions->nelts; ++i) {
            ap_rputs("  <tr>\n",r);
            jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                                   "%s",elts[i].key);
            jserv_status_out_table(r,"#e0e0e0","valign=top align=left",
                                   "%s",elts[i].val);
            ap_rputs("  </tr>\n",r);
            ap_reset_timeout(r);
        }
    }
    ap_rputs("</table>\n",r);

    /* Output Menu, Base server and next server links */
    ap_reset_timeout(r);
    ap_rputs("<br>\n",r);
    ap_rputs("<table border=0>\n",r);
    ap_rputs("  <tr>\n",r);
 
    jserv_status_out_table(r,"#ffffff","width=33% valign=top align=center",
    "<a href=\"./status?menu\">Back to menu</a>");

    if (jserv_servers!=NULL)
    jserv_status_out_table(r,"#ffffff","width=33% valign=top align=center",
    "<a href=\"./status?module=%s\">Base server <i>(%s)</i></a>",
                jserv_servers->server->server_hostname, 
                jserv_servers->server->server_hostname);

    if (cfg->next!=NULL)
    jserv_status_out_table(r,"#ffffff","width=33% valign=top align=center",
    "<a href=\"./status?module=%s\">Next server <i>(%s)</i></a>",
                cfg->next->server->server_hostname, 
                cfg->next->server->server_hostname);

    ap_rputs("  </tr>\n",r);
    ap_rputs("</table>\n",r);

    /* Done */
    ap_rputs("</center>\n",r);
    jserv_status_out_tail(r);
    return OK;
}

/* ========================================================================= */
/* Output the IMAGE */
static int jserv_status_out_image(jserv_config *cfg, jserv_request *req,
                                   request_rec *r) {
    r->content_type = "image/gif";
#ifdef CHARSET_EBCDIC
    /* For EBCDIC, set the auto-conversion flag now that the MIME type is set */
    ap_checkconv(r);
#endif /*CHARSET_EBCDIC*/
    ap_set_content_length(r,jserv_image_size);
    ap_send_http_header(r);
    if (r->header_only) return OK;

    /* Image output */
    ap_kill_timeout(r);
    ap_rwrite(jserv_image, jserv_image_size, r);
    return OK;
}

/* ========================================================================= */
/* Output the JServ status configuration calling JServ as a sevlet */
static int jserv_status_out_jserv(jserv_config *cfg, jserv_request *req,
                                   request_rec *r, int mount_number) {
     jserv_status_host *rcur=jserv_status_hosts;
     int tmp_int = 0;
 
     req->isdir=JSERV_FALSE;
     req->servlet=JSERV_SERVLET;
 
     while (tmp_int++ < mount_number && rcur != NULL) {
         rcur = rcur->next;
     }
 
     if (rcur == NULL) { /* weird -- we walked to the end.  oh well -- */
         return FORBIDDEN;     /* it's not currently a valid 'engine'. */
     } else {
         req->mount=rcur->mount;
         req->zone=rcur->mount->zone;
         return jserv_protocol_handler(rcur->mount->protocol, cfg, req, r);
     }
}

/* ========================================================================= */
/* Output the JServ status configuration calling JServ as a servlet */
static int jserv_status_out_baljserv(jserv_config *cfg, jserv_request *req,
                                   request_rec *r) {
    jserv_config *cur;
    char sta;
    char buffer1[128];
    char buffer2[128];
    char buffer3[128];
    int  job;
    int  ret;
    char *ptr;

    buffer1[0]='\0';
    buffer2[0]='\0';
    buffer3[0]='\0';
    if (strstr(r->uri, "/jserv/engine/direct/status"))  {
        ap_table_setn(r->headers_out, "Location", "/jserv/status?menu");
        return REDIRECT;
    }
    ret = sscanf(r->uri,
  "/jserv/engine/direct/%127[a-zA-Z1-9.-]/%127[a-zA-Z1-9.-]/%127[a-zA-Z1-9.-]",
          buffer1, buffer2, buffer3);
    if (ret == 3) {   
        if (strcmp(buffer3, JSERV_SERVLET))
            return FORBIDDEN;
    }  
    else if (ret != 2)    
        return FORBIDDEN;
    sta = '\0';
    job = 0;
    if (r->args)
        if (strstr(r->args, "changebutton=apply")) {
            /* we can't use ap_unescape_url here as our "/" is not valid in the result */
            if ((ptr=strstr(r->args, "newst=%2B"))!=0)
                sta='+';
            else if ((ptr=strstr(r->args, "newst=%2F"))!=0)
                sta='/';
            else if ((ptr=strstr(r->args, "newst=-"))!=0)
                sta='-';
            else if ((ptr=strstr(r->args, "newst=X"))!=0)
                sta='X';
            job = sta ? 2:0;
       }

    if (!r->args || strstr(r->args, "testbutton=test") ||  strstr(r->args, "status")  ||  strstr(r->args, "zones=") || strstr(r->args, "menu")) {
        job = 1;
    }
    if (job == 0)
        return FORBIDDEN;

    /* get the server entry */
    cur=jserv_servers;
    /* Set default return 500 (internal server error) */
    ret=SERVER_ERROR;
    /* Iterate thru all configurations to match server name */
    while (cur!=NULL) {
        if (cur->server!=NULL) {
            if (strcmp(cur->server->server_hostname,buffer1)==0) {
                /* we've got the server */
                jserv_mount *curm = cur->mount;
                while (curm != NULL) {
                    jserv_host *curh = curm->hosturls;
                    jserv_host *start = curh;
                    while (curh != NULL) {
                        if (strcmp(curh->id, buffer2)==0) {
                            if (job == 1)
                                ret=jserv_status_out_baljserv2(cfg,req,r,curm, curh);
                            if (job == 2) {
                                char new_url[512];
                                sprintf(new_url, "/jserv/status?module=%s", buffer1);
                                jserv_change_status(cfg, curh, sta);
                                ap_table_setn(r->headers_out, "Location", new_url);
                                return REDIRECT;
                            }
                            break;
                        }
                        curh = curh->next;
                        if (curh == start)
                            curh=NULL;
                    }
                    if (curh == NULL)
                        curm= curm->next;
                    else  break;
                }
                break; /* didn't find the id for this host (error) */
            }
        }
        cur=cur->next;
    }
    return ret;
}

static void jserv_change_status(jserv_config *cfg, jserv_host *cur, char sta) {
/*    switch (sta) {
      case '+':
      case '-':
      case '/':
      case 'X': break;
      default: break;
    } */
    jserv_changestate(cfg, cur, "+-/X", sta);
}

static int jserv_status_out_baljserv2(jserv_config *cfg, jserv_request *req,
                                   request_rec *r, jserv_mount *mnt, jserv_host *cur) {
     jserv_mount save;
     int ret;
     req->isdir=JSERV_FALSE;
     req->servlet=JSERV_SERVLET;
 
     if (cur == NULL) { /* weird -- we walked to the end.  oh well -- */
         return FORBIDDEN;     /* it's not currently a valid 'engine'. */
     } else {
         req->mount=mnt;
         req->zone=mnt->zone;
         save.protocol=req->mount->protocol;
         save.hostaddr=req->mount->hostaddr;
         save.port=req->mount->port;
         save.host=req->mount->host;
         save.secretfile=req->mount->secretfile;
         save.secret=req->mount->secret;
         save.secretsize=req->mount->secretsize;
         req->mount->protocol = cur->protocol;
         req->mount->hostaddr = cur->hostaddr;
         req->mount->port = cur->port;
         req->mount->host = cur->host;
         req->mount->secretfile = cur->secretfile;
         req->mount->secret = cur->secret;
         req->mount->secretsize = cur->secretsize;

         jserv_error(JSERV_LOG_DEBUG,cfg,
             "balance: %d call in server %s: %s://%s(%lx):%d",
             getpid(),
             cur->name, cur->protocol?cur->protocol->name:"DEFAULT",
             cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);
         ret = jserv_protocol_handler(cur->protocol, cfg, req, r);

         req->mount->protocol = save.protocol;
         req->mount->hostaddr = save.hostaddr;
         req->mount->port = save.port;
         req->mount->host = save.host;
         req->mount->secretfile = save.secretfile;
         req->mount->secret = save.secret;
         req->mount->secretsize = save.secretsize;
         if (ret != SERVER_ERROR && ret != HTTP_INTERNAL_SERVER_ERROR) {
             jserv_setalive(cfg, cur);
             return ret;
         } 
         if (r->status == SERVER_ERROR) {
             jserv_error(JSERV_LOG_ERROR,cfg,
             "balance: %d internal servlet error in server %s: %s://%s(%lx):%d",
             getpid(),
             cur->name, cur->protocol?cur->protocol->name:"DEFAULT",
             cur->host?cur->host:"DEFAULT", cur->hostaddr, cur->port);
             jserv_setalive(cfg, cur);
             return ret;
         }
         jserv_error(JSERV_LOG_INFO,cfg,
                     "balance: %d %s unsuccessfully ", getpid(), cur->id);
         jserv_setdead(cfg, cur);
         return ret;
     }
}
/*****************************************************************************
 * Dummy Protocol Structure definition                                       *
 *****************************************************************************/
jserv_protocol jserv_status = {
    "status",                           /* Name for this protocol */
    0,                                  /* Default port for this protocol */
    jserv_status_init,                  /* init() */
    NULL,                               /* cleanup() */
    NULL,                               /* child_init() */
    NULL,                               /* child_cleanup() */
    jserv_status_handler,               /* handler() */
    NULL,                               /* function() */
    NULL,                               /* parameter() */
};
