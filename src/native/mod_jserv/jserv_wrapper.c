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
 * Description: wrapper protocol: starts and controls the JVM used by JServ  *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/
#include "jserv_wrapper.h"
/* what should I do about Win32 re: time?? */
#include <time.h>
#include <limits.h>

int wrapper_restart_count = 0;
time_t wrapper_restart_last_time;

/*****************************************************************************
 * Utilities nedeed in wrapper                                               *
 *****************************************************************************/
/* The old wrapper would try to launch the JVM for a maximum of five times.
* If the JVM did not launch five times in a row it would just completely
* give up on it. This made my site very unreliable if a temporary mistake
* caused the JVM to fail to launch for a brief period of less than a minute
* The solution I came up with is to reduce the frequency with which the JVM
* is relaunched after some number of consecutive failures. The failure count
* is only reset if the JVM is  alive for a minimum amount of time. This limits
* CPU utilization while allowing Apache JServ some measure of automatic
* recovery. 
*
* The following structure contains information for keeping track of the
* current delay given the number of consecutive failures to launch the JVM.
* The default values are set up so as described by the following table:
*
*  consecutive   delay in
*  failures      seconds
*  -----------   --------
*  0-4             1
*  5-9            60
*  >9            300
*
* You can set this up to exit (give up) after some number of failures 
* by adding a final entry with a delay of zero seconds and a finite
* (<INT_MAX) failure count for the preceding entry. For instance, to get
* the old (1.0b3) behavior of exiting after 5 retries, use the values
* { 1, 0 } for the delays and { 4, INT_MAX } for the failure counts.
*/
#define NO_MORE_RETRIES (0)
#define INFINITE_FAILURES (INT_MAX)
struct {
	int counts[3]; /* number of failures before moving to next delay */
	int delays[3]; /* delays used if too many failures occur in a row */
	int index;     /* index to current delay/count */
	int failures;  /* number of consecutive failures */
	int started;   /* when wrapper was first successfully known to be running */
	int bigboy;    /* number of seconds before considering jvm successfully launched */ 
} wrapper_restart = {
	{ 5, 10, INFINITE_FAILURES },
	{ 1, 60, 300 },
	0,
	0,
	0,
	120
};

/* ========================================================================= */
/* Called when the wrapper detects that the JVM is not running. */
void wrapper_restart_failed() {
	wrapper_restart.started = 0;
	if (wrapper_restart.failures < INFINITE_FAILURES)
		wrapper_restart.failures++;
	if (wrapper_restart.failures >= wrapper_restart.counts[wrapper_restart.index]) {
		wrapper_restart.index++;
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
		  "wrapper: Java VM died %d times in a row with"
		  " less than %d seconds between successive failures,"
		  " setting delay to %d seconds before restarting."
		  " Check the Apache error log and the Apache JServ log files for more"
		  " details; for maximum log information be sure to enable the Apache "
		  " JServ log file in the main jserv properties file by: setting the "
		  " 'log' property to true, the 'log.file' property to a file writable "
		  " by the uid as whom Apache JServ is run, and by enabling log channels "
		  " using the 'log.channel.*' properties)",
          wrapper_restart.failures,
          wrapper_restart.bigboy,
          wrapper_restart_delay());
    }
}

/* ========================================================================= */
/* Called when the wrapper detects that the JVM is running. This resets the
* failure count if the JVM is alive for a minimum amount of time. */
void wrapper_restart_succeeded() {
	if (wrapper_restart.started == 0)
		wrapper_restart.started = time(NULL);
	if (time(NULL) - wrapper_restart.started > wrapper_restart.bigboy) {
		wrapper_restart.index = 0;
		wrapper_restart.failures = 0;
	}
}

/* ========================================================================= */
/* Returns the number of seconds to wait before attempting to restart the
 * wrapper, or zero to indicate that no more attempts should be made. */
int wrapper_restart_delay() {
	return wrapper_restart.delays[wrapper_restart.index];
}

/*****************************************************************************
 * In the interests of providing decent debugging output to users who have   *
 * difficulty setting up Apache-JServ, it's useful to dump debugging         *
 * information after sufficient failures.                                    *
 *****************************************************************************/
int wrapper_restart_want_debug_data() {
	return wrapper_restart.failures >=
		(wrapper_restart.counts[wrapper_restart.index] - 1);
}


/*****************************************************************************
 * This function is used to avoid endless spawn/die cycles. This is replaced *
 * by the three-function combo wrapper_restart_failed,                       *
 * wrapper_restart_succeeded, and wrapper_restart_delay, except that this is *
 * included for backwards-compatability with the win code which hasn't been  *
 * updated yet.                                                              *
 *****************************************************************************/
int wrapper_check_restart_time_ok() {
    time_t newtime = time(NULL);
    if (newtime - wrapper_restart_last_time < 5) {
        ++wrapper_restart_count;
    } else {
        wrapper_restart_count = 0;
    }
    wrapper_restart_last_time = newtime;
    if (wrapper_restart_count > 5) {
        jserv_error(JSERV_LOG_INFO,wrapper_data->config,
  "wrapper: VM died too many times w/in 5 second intervals (%d); no more tries",
            wrapper_restart_count);
        return 0;
    } else {
        return 1;
    }
}

/* ========================================================================= */
/* Parses a name=value line into a wrapper_property structure */
wrapper_property *wrapper_parseline(pool *p, char *line) {
    wrapper_property *prop=NULL;
    int len=strlen(line);
    char *buf;
    int y=0,x=0;

    /* Remove trailing newline */
    if (line[len-1]=='\n') line[--len]='\0';

    /* Remove trailing spaces */
    x=len;
    while ((x>0) && (line[x-1]==' ')) x--;
    line[x]='\0';

    /* Remove leading spaces */
    x=0;
    while ((x<len) && (line[x]==' ')) x++;
    buf=&line[x];
    len=strlen(buf);

    /* Check for comments or empty lines */
    if (len==0) return NULL;
    if ((buf[0]=='#') || (buf[0]=='\n') || (buf[0]=='\0')) return NULL;

    /* We have something, allocate space */
    prop=(wrapper_property *)ap_pcalloc(p, sizeof(wrapper_property));

    /* Check for equal sign */
    while ((x<len) && (buf[x]!='=')) x++;

    /* Remove trailing spaces from name */
    y=x;
    while ((buf[y-1]=='=') || (buf[y-1]==' ')) y--;

    /* Put data into structures */
    if (buf[x]=='=') {
        prop->name=ap_pstrndup(p, buf, y);

        /* Remove leading spaces or equal sign from value */
        while ((buf[x]=='=') || (buf[x]==' ')) x++;

        /* Check if value was empty */
        if (buf[x]!='\0') prop->value=ap_pstrdup(p, &buf[x]);
        else prop->value=NULL;
        prop->next=NULL;
    } else {
        prop->name=ap_pstrndup(p, buf,y);
        prop->value=NULL;
        prop->next=NULL;
    }

    /* Return the property we found */
    return prop;
}

/* ========================================================================= */
/* Adds sparator+value to a specified environment value */
void wrapper_env_concat(wrapper_config *cfg, char *name, char *value) {
    wrapper_property *cur;

    if (value==NULL) return;

    /* Check if we previously declared the same environment variable*/
    if (cfg->environment!=NULL) {
        cur=cfg->environment;
        while (cur!=NULL) {
            /* We matched the same variable */
#ifdef WIN32
            if (strcasecmp(cur->name,name)==0) {
#else /* ifdef WIN32 */
            if (strcmp(cur->name,name)==0) {
#endif /* ifdef WIN32 */
                cur->value=ap_psprintf(wrapper_pool, "%s%c%s", cur->value,
                                       WRAPPER_PATH_SEPARATOR, value);
                return;
            }
            cur=cur->next;
        }
    }

    /* If we arrive here no variable was found */
    cur=(wrapper_property *)ap_pcalloc(wrapper_pool,sizeof(wrapper_property));
    cur->next=cfg->environment;
    cfg->environment=cur;
    cur->name=ap_pstrdup(wrapper_pool,name);
    cur->value=ap_pstrdup(wrapper_pool,value);
}

/* ========================================================================= */
/* Replace value into a specified environment value */
void wrapper_env_replace(wrapper_config *cfg, char *name, char *value, int f) {
    wrapper_property *cur;

    if (value==NULL) return;

    /* Check if we previously declared the same environment variable*/
    if (cfg->environment!=NULL) {
        cur=cfg->environment;
        while (cur!=NULL) {
            /* We matched the same variable */
#ifdef WIN32
            if (strcasecmp(cur->name,name)==0) {
#else /* ifdef WIN32 */
            if (strcmp(cur->name,name)==0) {
#endif /* ifdef WIN32 */
                if (f==JSERV_TRUE) cur->value=ap_pstrdup(wrapper_pool,value);
                return;
            }
            cur=cur->next;
        }
    }

    /* If we arrive here no variable was found */
    cur=(wrapper_property *)ap_pcalloc(wrapper_pool,sizeof(wrapper_property));
    cur->next=cfg->environment;
    cfg->environment=cur;
    cur->name=ap_pstrdup(wrapper_pool,name);
    cur->value=ap_pstrdup(wrapper_pool,value);
}


/*****************************************************************************
 * Configuration procedures                                                  *
 *****************************************************************************/

/* ========================================================================= */
/* Handle wrapper.bin property */
static char *wrapper_config_bin(wrapper_config *cfg, char *value) {
    if (value==NULL) return "wrapper.bin must be called with a parameter";
    if (cfg->bin!=NULL) return "wrapper.bin specified twice";

    cfg->bin=ap_pstrdup(wrapper_pool,value);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.bin.parameters property */
static char *wrapper_config_bin_parameters(wrapper_config *cfg, char *value) {
    wrapper_property_list_node * binparams = cfg->binparam;
    char * temp;

    if (value==NULL || *value == '\0')
        return "wrapper.bin.parameters must be called with a parameter";

    if (binparams == NULL) {
        binparams = ap_palloc(wrapper_pool,
                              sizeof(wrapper_property_list_node));
        binparams->name = NULL;
        binparams->next = NULL;
        cfg->binparam = binparams;
    } else {
        while (binparams->next != NULL) binparams = binparams->next;
        if (binparams->name != NULL) {
            binparams->next = ap_palloc(wrapper_pool,
                                        sizeof(wrapper_property_list_node));
            binparams = binparams->next;
        }
    }

    temp = ap_pstrdup(wrapper_pool,value);
    while (*temp != '\0') {
        binparams->name = ap_getword_conf_nc(wrapper_pool, &temp);
        binparams->next = ap_palloc(wrapper_pool,
                                    sizeof(wrapper_property_list_node));
        binparams = binparams->next;
    }
    binparams->next = NULL;
    binparams->name = NULL;

    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.class property */
static char *wrapper_config_class(wrapper_config *cfg, char *value) {
    if (value==NULL) return "wrapper.class must be called with a parameter";
    if (cfg->class!=NULL) return "wrapper.class specified twice";

    cfg->class=ap_pstrdup(wrapper_pool,value);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.class.parameters property */
static char *wrapper_config_class_parameters(wrapper_config *cfg, char *value) {
    if (value==NULL) 
        return "wrapper.class.parameters must be called with a parameter";
    if (cfg->classparam!=NULL) return "wrapper.class.parameters specified twice";

    cfg->classparam=ap_pstrdup(wrapper_pool,value);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.path property */
static char *wrapper_config_path(wrapper_config *cfg, char *value) {
    if (value==NULL) return "wrapper.path must be called with a parameter";

    wrapper_env_concat(cfg, "PATH", value);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.classpath property */
static char *wrapper_config_classpath(wrapper_config *cfg, char *value) {
    if (value==NULL) /* from a line w/ just 'wrapper.classpath=' -- */
		return NULL; /* configure produces lines like that. */

    wrapper_env_concat(cfg, "CLASSPATH", value);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.env property */
static char *wrapper_config_env(wrapper_config *cfg, char *value) {
    wrapper_property *prop=NULL;

    if (value==NULL)
        return "wrapper.env must be called with a parameter";

    /* Parse our name=value in the way we do with properties file */
    prop=wrapper_parseline(wrapper_pool,value);

    if (prop==NULL) return "wrapper.env called with wrong parameter";
    if (prop->value==NULL) return "wrapper.env called with no variable value";

    wrapper_env_replace(cfg, prop->name, prop->value, JSERV_TRUE);
    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.env.copy property */
static char *wrapper_config_env_copy(wrapper_config *cfg, char *variable) {
    char *value;

    if ((value=getenv(variable))==NULL)
        return "wrapper.env.copy environment variable not found";

    /* Check if we had a path or classpath copy request */
#ifdef WIN32
    if ((strcasecmp(variable,"PATH")==0)|(strcasecmp(variable,"CLASSPATH")==0))
#else /* ifdef WIN32 */
    if ((strcmp(variable,"PATH")==0)|(strcmp(variable,"CLASSPATH")==0))
#endif /* ifdef WIN32 */
        wrapper_env_concat(cfg,variable,value);

    /* Other variables (rather than path or classpath should be overwritten */
    else
        wrapper_env_replace(cfg,variable,value,JSERV_TRUE);

    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.env.copyall property */
static char *wrapper_config_env_copyall(wrapper_config *cfg, char *value) {
    wrapper_property *prop=NULL;
    int x=0;

    if (value==NULL) 
        return "wrapper.env.copyall must be called with a parameter";

    if (strcasecmp(value,"TRUE")!=0) return NULL;

    x=0;
    while (environ[x]!=NULL) {
        prop=wrapper_parseline(wrapper_pool,environ[x]);
        if (prop!=NULL) {
#ifdef WIN32
            if ((strcasecmp(prop->name,"PATH")==0)|
                (strcasecmp(prop->name,"CLASSPATH")==0))
#else /* ifdef WIN32 */
            if ((strcmp(prop->name,"PATH")==0)|
                (strcmp(prop->name,"CLASSPATH")==0))
#endif /* ifdef WIN32 */
                wrapper_env_concat(cfg,prop->name,prop->value);
            else
                wrapper_env_replace(cfg,prop->name,prop->value,JSERV_FALSE);
        }
        x++;
    }

    return NULL;
}

/* ========================================================================= */
/* Handle port property */
static char *wrapper_config_port(wrapper_config *cfg, char *value) {
    /* Check if called with no value */
    if (value==NULL)
        return "port must be called with a parameter";

    /* Check if port was already specified */
    if (cfg->config->port!=0) return "port specified twice";

    /* Setup port */
    cfg->config->port=atoi(value);

    /* Check if found a valid value */
    if (cfg->config->port==0) return "port must specify a numeric value";

    return NULL;
}

/* ========================================================================= */
/* Handle security.authentication property */
static char *wrapper_config_auth(wrapper_config *cfg, char *value) {
    /* Check if called with no value */
    if (value==NULL)
        return "security.authentication must be called with a parameter";

    /* Check if port was already specified */
    if (cfg->config->secretsize!=0) 
        return "security.authentication specified twice";

    /* Checl for TRUE/FALSE */
    if (strcasecmp("true",value)==0) cfg->config->secretsize=JSERV_DEFAULT;
    else if (strcasecmp("false",value)==0)
        cfg->config->secretsize=JSERV_DISABLED;
    else return "port must be TRUE or FALSE";

    return NULL;
}

/* ========================================================================= */
/* Handle security.secretKey property */
static char *wrapper_config_secret(wrapper_config *cfg, char *value) {
    /* Check if called with no value */
    if (value==NULL)
        return "security.secretKey must be called with a parameter";

    /* Check if port was already specified */
    if (cfg->config->secretfile!=NULL) 
        return "security.secretKey specified twice";

    /* Set-up our filename */
    cfg->config->secretfile=ap_pstrdup(wrapper_pool,value);

    return NULL;
}

/* ========================================================================= */
/* Handle wrapper.protocol property */
static char *wrapper_config_protocol(wrapper_config *cfg, char *value) {
    /* Check if called with no value */
    if (value==NULL)
        return "wrapper.protocol must be called with a parameter";

    /* Check if port was already specified */
    if (cfg->config->protocol!=NULL) 
        return "wrapper.protocol specified twice";

    /* Set-up our filename */
    cfg->config->protocol=jserv_protocol_getbyname(value);
    if (cfg->config->protocol==NULL) return "protocol not found";

    return NULL;
}


/*****************************************************************************
 * Properties file parser and default values setup                           *
 *****************************************************************************/

/* ========================================================================= */
/* Setup defaults for specified configuration member */
char *wrapper_defaults(wrapper_config *cfg, pool *p) {
    char *ret;
#ifdef WIN32
    char *windir;
    char *winsys;
#endif /* ifdef WIN32 */

    /* Set defaults for JVM and CLASS values */
    if (cfg->bin==NULL)
        cfg->bin=ap_pstrdup(wrapper_pool,WRAPPER_DEFAULT_BIN);
 
    if (cfg->binparam==NULL)
        wrapper_config_bin_parameters(cfg, WRAPPER_DEFAULT_BINPARAM);

    if (cfg->class==NULL)
        cfg->class=ap_pstrdup(wrapper_pool,WRAPPER_DEFAULT_CLASS);
    if (cfg->classparam==NULL)
        cfg->classparam=ap_pstrdup(wrapper_pool,WRAPPER_DEFAULT_CLASSPARAM);

    /* Check paths */
    wrapper_env_concat(cfg,"PATH",WRAPPER_DEFAULT_PATH);

#ifdef WIN32
    /* Allocate value for Windows and Windows/System directories */
    windir=ap_pcalloc(p,MAX_PATH);
    winsys=ap_pcalloc(p,MAX_PATH);

    /* Get values of Windows and Windows/System directories */
    GetWindowsDirectory(windir,MAX_PATH);
    GetSystemDirectory(winsys,MAX_PATH);

    /* Setup path environment variable */
    wrapper_env_concat(cfg, "PATH", windir);
    wrapper_env_concat(cfg, "PATH", winsys);

    /* Windows need SystemDrive and SystemRoot environment variables */
    wrapper_env_replace(cfg,"SystemDrive",
                        ap_pstrndup(p,windir,2),JSERV_FALSE);
    wrapper_env_replace(cfg,"SystemRoot",
                        ap_pstrdup(p,windir),JSERV_FALSE);

#endif /* ifdef WIN32 */
    /* Check protocol for signalling */
    if (cfg->config->protocol==NULL)
        cfg->config->protocol=jserv_protocol_getbyname(JSERV_DEFAULT_PROTOCOL);
    if (cfg->config->protocol==NULL)
        return "default protocol not found";

    /* Check if localhost address was resolved */
    if (cfg->config->hostaddr==0) return "localhost address not resolved";

    /* Check port */
    if (cfg->config->port==0) return "port unspecified";

    /* Check and load secret */
    if (cfg->config->secretsize!=JSERV_DISABLED) {
        ret=(char *)jserv_readfile(wrapper_pool,cfg->config->secretfile,
                    JSERV_TRUE,&cfg->config->secret,&cfg->config->secretsize);
        if (ret!=NULL) {
            char * description =
                ap_psprintf(p, "error reading secret key file: %s", ret);
            return description;
        }
    }

    return NULL;
}

/* ========================================================================= */
/* Read the whole properties file and set up the configuration member */
int wrapper_parse(wrapper_config *cfg) {
    wrapper_property *prop=NULL;
    pool *p=ap_make_sub_pool(wrapper_pool);
    FILE *file=NULL;
    char *line=ap_pcalloc(p,1024);
    char *tmp=NULL;
    char *ret;
    int linenum=1;

    /* Check if filename is null */
    if (cfg->config->properties==NULL) {
        jserv_error(JSERV_LOG_ERROR,cfg->config,"wrapper: %s",
                                "must specify a properties file name");
        return -1;
    }

    /* Check wether we can open properties file */
    file=fopen(cfg->config->properties,"r");
    if (file==NULL) {
        jserv_error(JSERV_LOG_ERROR,cfg->config,"wrapper: cannot open %s file",
                    cfg->config->properties);
        return -1;
    }

    /* Parse the whole file */
    while (!(feof(file))) {
        /* Get a string and parse it into a wrapper_property structure */
        tmp=fgets(line,1024,file);
        if (tmp!=NULL) prop=wrapper_parseline(p,tmp);
        /* If we got a wrapper_property, check it out */
        if (prop!=NULL) {
            int x=0;
            /* Run thru our list of handlers and pass properties */
            while (wrapper_handlers[x].name!=NULL) {
                /* Check property name against our list */
                if (strcmp(wrapper_handlers[x].name,prop->name)==0)
                    /* Check wether handler routine is valid */
                    if (wrapper_handlers[x].routine!=NULL) {
                        /* Call handler and check for returned data */
                        ret=(wrapper_handlers[x].routine) (cfg,prop->value);
                        if (ret!=NULL) {
                            jserv_error(JSERV_LOG_ERROR,cfg->config,
                                        "wrapper: file %s (line %d) %s",
                                        cfg->config->properties,linenum,ret);
                            fclose(file);
                            return -1;
                        }
                    }
                /* Try next handler */
                x++;
            }
        }
        /* Increase line number */
        linenum++;
    }

    /* Setup default values */
    ret=wrapper_defaults(cfg,p);
    if (ret!=NULL) {
        jserv_error(JSERV_LOG_ERROR,cfg->config,"wrapper: setting defaults %s",
                    ret);
        fclose(file);
        return -1;
    }

    /* Destroy our memory pool and close file */
    fclose(file);
    return 0;
}

/*****************************************************************************
 * Protocol startup/shutdown entry point                                     *
 *****************************************************************************/
pool *wrapper_pool;
wrapper_config *wrapper_data=NULL;

/* ========================================================================= */
/* Initializes the protocol */
static int wrapper_init(jserv_config *cfg) {
    /* System dependant create all */
    if (cfg->manual==JSERV_TRUE) return 0;
    else {
        int ret;
        wrapper_pool=ap_make_sub_pool(jserv_pool);
        wrapper_data=(wrapper_config *)ap_pcalloc(wrapper_pool,
                                                  sizeof(wrapper_config));
        wrapper_data->config=(jserv_config *)ap_pcalloc(wrapper_pool,
                                                        sizeof(jserv_config));

        /* Put inherited values in wrapper_data->config */
        /* Place our server_rec structure */
        wrapper_data->config->server=cfg->server;
        /* Be sure we are in AUTOMATIC MODE */
        wrapper_data->config->manual=JSERV_FALSE;
        /* Place our properties file name */
        wrapper_data->config->properties=ap_pstrdup(wrapper_pool,
                                                    cfg->properties);
        /* Protocol will be resolved further on */
        wrapper_data->config->protocol=NULL;
        /* Host is LOCALHOST (jserv is started here) */
        wrapper_data->config->host=ap_pstrdup(wrapper_pool,cfg->host);
        wrapper_data->config->hostaddr=jserv_resolve(wrapper_data->config->host);
        /* Port will be resolved further on */
        wrapper_data->config->port=0;
        /* Define NO MOUNTPOINTS for this */
        wrapper_data->config->mount=NULL;
        wrapper_data->config->mountcopy=JSERV_FALSE;
        /* Keep our previous log file */
        wrapper_data->config->logfile=ap_pstrdup(wrapper_pool,cfg->logfile);
        wrapper_data->config->logfilefd=cfg->logfilefd;
        wrapper_data->config->loglevel=cfg->loglevel;
        /* Secrets are resolved further on */
        wrapper_data->config->secretfile=NULL;
        wrapper_data->config->secret=NULL;
        wrapper_data->config->secretsize=0;
        /* these are used only by the wrapper */
        wrapper_data->config->vmtimeout=cfg->vmtimeout;
        wrapper_data->config->vminterval=cfg->vminterval;
        wrapper_data->config->retryattempts=cfg->retryattempts;

        /* This is the only jserv_config element */
        wrapper_data->config->next=NULL;
        

        /* Parse properties file */
        ret=wrapper_parse(wrapper_data);
        if (ret!=0) return -1;

        /* Try startup */
        ret=wrapper_create(wrapper_data);
        if (ret!=0) return -1;
    }
    return 0;
}

/* ========================================================================= */
/* Cleans up the protocol */
int wrapper_cleanup(jserv_config *cfg) {
    /* System dependant create all */
    if (cfg->manual==JSERV_TRUE) return 0;
    else {
        int ret;
        if (wrapper_data!=NULL)
            ret=wrapper_destroy(wrapper_data);
        else ret=-1;
  
        return ret;
    }
}


/*****************************************************************************
 * Wrapper Protocol Structure definition                                     *
 *****************************************************************************/

/* ========================================================================= */
/* The wrapper protocol */
jserv_protocol jserv_wrapper = {
    "wrapper",                  /* Name for this protocol */
    0,                          /* Default port for this protocol */
    wrapper_init,               /* init() */
    wrapper_cleanup,            /* cleanup() */
    NULL,                       /* child_init() */
    NULL,                       /* child_cleanup() */
    NULL,                       /* handler() */
    NULL,                       /* function() */
    NULL,                       /* parameter() */
};

/* ========================================================================= */
/* Properties handled by wrapper, found in jserv.properties */
wrapper_property_handler wrapper_handlers[] = {
    { "wrapper.bin", wrapper_config_bin },
    { "wrapper.bin.parameters", wrapper_config_bin_parameters },
    { "wrapper.class", wrapper_config_class },
    { "wrapper.class.parameters", wrapper_config_class_parameters },
    { "wrapper.path", wrapper_config_path },
    { "wrapper.classpath", wrapper_config_classpath },
    { "wrapper.env", wrapper_config_env },
    { "wrapper.env.copy", wrapper_config_env_copy },
    { "wrapper.env.copyall", wrapper_config_env_copyall },
    /* Things for signals to place in wrapper_data->config */
    { "port", wrapper_config_port },
    { "security.authentication", wrapper_config_auth },
    { "security.secretKey", wrapper_config_secret },
    { "wrapper.protocol", wrapper_config_protocol },
    { NULL }
};

#ifdef JSERV_STANDALONE
/*****************************************************************************
 * Standalone wrapper procedures                                             *
 *****************************************************************************/

/* ========================================================================= */
/* Our main() */
void main(int argc, char *argv[]) {
    jserv_config *cfg;
    int ret;

    /* Check if we had a configuration file */
    if (argc!=2) {
        printf("%s: usage: \"%s properties_file_name\"\n",argv[0],argv[0]);
        exit (1);
    }

    /* Os dependand startup code */
    wrapper_standalone_startup();

    /* Allocate memory */
    jserv_pool=ap_make_sub_pool(NULL);
    cfg=(jserv_config *)ap_pcalloc(jserv_pool,sizeof(jserv_config));

    /* Set our memory pool in config and our configuration member */
    cfg->server=NULL;
    cfg->manual=JSERV_FALSE;
    cfg->properties=argv[1];
    cfg->protocol=NULL;
    cfg->host=NULL;
    cfg->hostaddr=0;
    cfg->port=0;
    cfg->mount=NULL;
    cfg->mountcopy=JSERV_FALSE;
    cfg->logfile=NULL;
    cfg->logfilefd=JSERV_DISABLED;
    cfg->secretfile=NULL;
    cfg->secret=NULL;
    cfg->secretsize=0;
    cfg->next=NULL;

    /* Try first startup ("call it defensive programming" and check config) */
    ret=wrapper_init(cfg);
    if (ret!=0) exit(1);
    ret=wrapper_cleanup(cfg);
    if (ret!=0) exit(1);

    /* Second (and final) startup */
    ret=wrapper_init(cfg);
    if (ret!=0) exit(1);

    /* Os dependand process code */
    wrapper_standalone_process();
}

#endif /* ifdef JSERV_STANDALONE */
