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

/***************************************************************************
 * Description: ISAPI plugin for IIS/PWS                                   *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Author:      Ignacio J. Ortega <nacho@apache.org>                       *
 * Version:     $Revision: 1.7 $                                           *
 ***************************************************************************/

// This define is needed to include wincrypt,h, needed to get client certificates
#define _WIN32_WINNT 0x0400

#include <httpext.h>
#include <httpfilt.h>
#include <wininet.h>

#include "jk_global.h"
#include "jk_util.h"
#include "jk_map.h"
#include "jk_pool.h"
#include "jk_service.h"
#include "jk_worker.h"
#include "jk_ajp12_worker.h"
#include "jk_uri_worker_map.h"

#define VERSION_STRING "Jakarta/ISAPI/1.0b1"

/*
 * We use special two headers to pass values from the filter to the 
 * extension. These values are:
 *
 * 1. The real URI before redirection took place
 * 2. The name of the worker to be used.
 *
 */
#define URI_HEADER_NAME         ("TOMCATURI:")
#define WORKER_HEADER_NAME      ("TOMCATWORKER:")
#define CONTENT_LENGTH          ("CONTENT_LENGTH:")

#define HTTP_URI_HEADER_NAME     ("HTTP_TOMCATURI")
#define HTTP_WORKER_HEADER_NAME  ("HTTP_TOMCATWORKER")

#define REGISTRY_LOCATION       ("Software\\Apache Software Foundation\\Jakarta Isapi Redirector\\1.0")
#define EXTENSION_URI_TAG       ("extension_uri")

#define GET_SERVER_VARIABLE_VALUE(name, place) {    \
    (place) = NULL;                                   \
    huge_buf_sz = sizeof(huge_buf);                 \
    if(get_server_value(private_data->lpEcb,        \
                        (name),                     \
                        huge_buf,                   \
                        huge_buf_sz,                \
                        "")) {                      \
        (place) = jk_pool_strdup(&private_data->p, huge_buf);   \
    }   \
}\

#define GET_SERVER_VARIABLE_VALUE_INT(name, place, def) {   \
    huge_buf_sz = sizeof(huge_buf);                 \
    if(get_server_value(private_data->lpEcb,        \
                        (name),                     \
                        huge_buf,                   \
                        huge_buf_sz,                \
                        "")) {                      \
        (place) = atoi(huge_buf);                   \
        if(0 == (place)) {                          \
            (place) = def;                          \
        }                                           \
    } else {    \
        (place) = def;  \
    }           \
}\

static char ini_file_name[MAX_PATH];
static int using_ini_file = JK_FALSE;
static int   is_inited = JK_FALSE;
static jk_uri_worker_map_t *uw_map = NULL; 
static jk_logger_t *logger = NULL; 

static char extension_uri[INTERNET_MAX_URL_LENGTH] = "/jakarta/isapi_redirect.dll";
static char log_file[MAX_PATH * 2];
static int  log_level = JK_LOG_EMERG_LEVEL;
static char worker_file[MAX_PATH * 2];
static char worker_mount_file[MAX_PATH * 2];

struct isapi_private_data {
    jk_pool_t p;
    
    int request_started;
    unsigned bytes_read_so_far;
    LPEXTENSION_CONTROL_BLOCK  lpEcb;
};
typedef struct isapi_private_data isapi_private_data_t;


static int JK_METHOD start_response(jk_ws_service_t *s,
                                    int status,
                                    const char *reason,
                                    const char * const *header_names,
                                    const char * const *header_values,
                                    unsigned num_of_headers);

static int JK_METHOD read(jk_ws_service_t *s,
                          void *b,
                          unsigned l,
                          unsigned *a);

static int JK_METHOD write(jk_ws_service_t *s,
                           const void *b,
                           unsigned l);

static int init_ws_service(isapi_private_data_t *private_data,
                           jk_ws_service_t *s,
                           char **worker_name);

static int initialize_extension(void);

static int read_registry_init_data(void);

static int get_registry_config_parameter(HKEY hkey,
                                         const char *tag, 
                                         char *b,
                                         DWORD sz);


static int get_server_value(LPEXTENSION_CONTROL_BLOCK lpEcb,
                            char *name,
                            char  *buf,
                            DWORD bufsz,
                            char  *def_val);

static int base64_encode_cert_len(int len);

static int base64_encode_cert(char *encoded,
                              const unsigned char *string,
                              int len);


static int uri_is_web_inf(char *uri)
{
    char *c = uri;
    while(*c) {
        *c = tolower(*c);
        c++;
    }                    
    if(strstr(uri, "web-inf")) {
        return JK_TRUE;
    }

    return JK_FALSE;
}

static int JK_METHOD start_response(jk_ws_service_t *s,
                                    int status,
                                    const char *reason,
                                    const char * const *header_names,
                                    const char * const *header_values,
                                    unsigned num_of_headers)
{
    static char crlf[3] = { (char)13, (char)10, '\0' };

    jk_log(logger, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::start_response\n");

    if(status < 100 || status > 1000) {
        jk_log(logger, JK_LOG_ERROR, 
               "jk_ws_service_t::start_response, invalid status %d\n", status);
        return JK_FALSE;
    }

    if(s && s->ws_private) {
        isapi_private_data_t *p = s->ws_private;
        if(!p->request_started) {
            DWORD len_of_status;
            char *status_str;
            char *headers_str;

            p->request_started = JK_TRUE;

            /*
             * Create the status line
             */
            if(!reason) {
                reason = "";
            }
            status_str = (char *)_alloca((6 + strlen(reason)) * sizeof(char));
            sprintf(status_str, "%d %s", status, reason);
            len_of_status = strlen(status_str); 
        
            /*
             * Create response headers string
             */
            if(num_of_headers) {
                unsigned i;
                unsigned len_of_headers;
                for(i = 0 , len_of_headers = 0 ; i < num_of_headers ; i++) {
                    len_of_headers += strlen(header_names[i]);
                    len_of_headers += strlen(header_values[i]);
                    len_of_headers += 4; /* extra for colon, space and crlf */
                }

                len_of_headers += 3;  /* crlf and terminating null char */
                headers_str = (char *)_alloca(len_of_headers * sizeof(char));
                headers_str[0] = '\0';

                for(i = 0 ; i < num_of_headers ; i++) {
                    strcat(headers_str, header_names[i]);
                    strcat(headers_str, ": ");
                    strcat(headers_str, header_values[i]);
                    strcat(headers_str, crlf);
                }
                strcat(headers_str, crlf);
            } else {
                headers_str = crlf;
            }

            if(!p->lpEcb->ServerSupportFunction(p->lpEcb->ConnID, 
                                                HSE_REQ_SEND_RESPONSE_HEADER,
                                                status_str,
                                                (LPDWORD)&len_of_status,
                                                (LPDWORD)headers_str)) {
                jk_log(logger, JK_LOG_ERROR, 
                       "jk_ws_service_t::start_response, ServerSupportFunction failed\n");
                return JK_FALSE;
            }       


        }
        return JK_TRUE;

    }

    jk_log(logger, JK_LOG_ERROR, 
           "jk_ws_service_t::start_response, NULL parameters\n");

    return JK_FALSE;
}

static int JK_METHOD read(jk_ws_service_t *s,
                          void *b,
                          unsigned l,
                          unsigned *a)
{
    jk_log(logger, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::read\n");

    if(s && s->ws_private && b && a) {
        isapi_private_data_t *p = s->ws_private;
        
        *a = 0;
        if(l) {
            char *buf = b;
            DWORD already_read = p->lpEcb->cbAvailable - p->bytes_read_so_far;
            
            if(already_read >= l) {
                memcpy(buf, p->lpEcb->lpbData + p->bytes_read_so_far, l);
                p->bytes_read_so_far += l;
                *a = l;
            } else {
                /*
                 * Try to copy what we already have 
                 */
                if(already_read > 0) {
                    memcpy(buf, p->lpEcb->lpbData + p->bytes_read_so_far, already_read);
                    buf += already_read;
                    l   -= already_read;
                    p->bytes_read_so_far = p->lpEcb->cbAvailable;
                    
                    *a = already_read;
                }
                
                /*
                 * Now try to read from the client ...
                 */
                if(p->lpEcb->ReadClient(p->lpEcb->ConnID, buf, &l)) {
                    *a += l;            
                } else {
                    jk_log(logger, JK_LOG_ERROR, 
                           "jk_ws_service_t::read, ReadClient failed\n");
                    return JK_FALSE;
                }                   
            }
        }
        return JK_TRUE;
    }

    jk_log(logger, JK_LOG_ERROR, 
           "jk_ws_service_t::read, NULL parameters\n");
    return JK_FALSE;
}

static int JK_METHOD write(jk_ws_service_t *s,
                           const void *b,
                           unsigned l)
{
    jk_log(logger, JK_LOG_DEBUG, 
           "Into jk_ws_service_t::write\n");

    if(s && s->ws_private && b) {
        isapi_private_data_t *p = s->ws_private;

        if(l) {
            unsigned written = 0;           
            char *buf = (char *)b;

            if(!p->request_started) {
                start_response(s, 200, NULL, NULL, NULL, 0);
            }

            while(written < l) {
                DWORD try_to_write = l - written;
                if(!p->lpEcb->WriteClient(p->lpEcb->ConnID, 
                                          buf + written, 
                                          &try_to_write, 
                                          0)) {
                    jk_log(logger, JK_LOG_ERROR, 
                           "jk_ws_service_t::write, WriteClient failed\n");
                    return JK_FALSE;
                }
                written += try_to_write;
            }
        }

        return JK_TRUE;

    }

    jk_log(logger, JK_LOG_ERROR, 
           "jk_ws_service_t::write, NULL parameters\n");

    return JK_FALSE;
}

BOOL WINAPI GetFilterVersion(PHTTP_FILTER_VERSION pVer)
{
    ULONG http_filter_revision = HTTP_FILTER_REVISION 

    pVer->dwFilterVersion = pVer->dwServerFilterVersion;
                        
    if(pVer->dwFilterVersion > http_filter_revision) {
        pVer->dwFilterVersion = http_filter_revision;
    }

    pVer->dwFlags = SF_NOTIFY_ORDER_HIGH        | 
                    SF_NOTIFY_SECURE_PORT       | 
                    SF_NOTIFY_NONSECURE_PORT    |
                    SF_NOTIFY_PREPROC_HEADERS;
                    
    strcpy(pVer->lpszFilterDesc, VERSION_STRING);

    if(!is_inited) {
        return initialize_extension();
    }

    return TRUE;
}

DWORD WINAPI HttpFilterProc(PHTTP_FILTER_CONTEXT pfc,
                            DWORD dwNotificationType, 
                            LPVOID pvNotification)
{
    if(is_inited &&
       (SF_NOTIFY_PREPROC_HEADERS == dwNotificationType)) { 
        PHTTP_FILTER_PREPROC_HEADERS p = (PHTTP_FILTER_PREPROC_HEADERS)pvNotification;
        char uri[INTERNET_MAX_URL_LENGTH];
		char snuri[INTERNET_MAX_URL_LENGTH]="/";
		char Host[INTERNET_MAX_URL_LENGTH];

        char *query;
        DWORD sz = sizeof(uri);
        DWORD szHost = sizeof(Host);

        jk_log(logger, JK_LOG_DEBUG, 
               "HttpFilterProc started\n");

        /*
         * Just in case somebody set these headers in the request!
         */
        p->SetHeader(pfc, URI_HEADER_NAME, NULL);
	    p->SetHeader(pfc, WORKER_HEADER_NAME, NULL);
        
        if(!p->GetHeader(pfc, "url", (LPVOID)uri, (LPDWORD)&sz)) {
            jk_log(logger, JK_LOG_ERROR, 
                   "HttpFilterProc error while getting the url\n");
            return SF_STATUS_REQ_ERROR;
        }

        if(strlen(uri)) {
            char *worker=0;
            query = strchr(uri, '?');
            if(query) {
                *query = '\0';
            }
			if(p->GetHeader(pfc, "Host:", (LPVOID)Host, (LPDWORD)&szHost)) {
				strcat(snuri,Host);
				strcat(snuri,uri);
				jk_log(logger, JK_LOG_DEBUG, 
					   "In HttpFilterProc Virtual Host redirection of %s\n", 
					   snuri);
				worker = map_uri_to_worker(uw_map, snuri, logger);                
			}
			if (!worker) {
				jk_log(logger, JK_LOG_DEBUG, 
					   "In HttpFilterProc test Default redirection of %s\n", 
					   uri);
				worker = map_uri_to_worker(uw_map, uri, logger);
			}
            if(query) {
                *query = '?';
            }

            if(worker) {
                /* This is a servlet, should redirect ... */
                jk_log(logger, JK_LOG_DEBUG, 
                       "HttpFilterProc [%s] is a servlet url - should redirect to %s\n", 
                       uri, worker);

                
				if(!p->AddHeader(pfc, URI_HEADER_NAME, uri) || 
                   !p->AddHeader(pfc, WORKER_HEADER_NAME, worker) ||
                   !p->SetHeader(pfc, "url", extension_uri)) {
                    jk_log(logger, JK_LOG_ERROR, 
                           "HttpFilterProc error while adding request headers\n");
                    return SF_STATUS_REQ_ERROR;
                }
            } else {
                jk_log(logger, JK_LOG_DEBUG, 
                       "HttpFilterProc [%s] is not a servlet url\n", 
                       uri);
            }

            /*
             * Check if somebody is feading us with his own TOMCAT data headers.
             * We reject such postings !
             */
            jk_log(logger, JK_LOG_DEBUG, 
                   "HttpFilterProc check if [%s] is points to the web-inf directory\n", 
                   uri);

            if(uri_is_web_inf(uri)) {
                char crlf[3] = { (char)13, (char)10, '\0' };
                char ctype[30];
                char *msg = "<HTML><BODY><H1>Access is Forbidden</H1></BODY></HTML>";
                DWORD len = strlen(msg);

                jk_log(logger, JK_LOG_EMERG, 
                       "HttpFilterProc [%s] points to the web-inf directory.\nSomebody try to hack into the site!!!\n", 
                       uri);

                sprintf(ctype, 
                        "Content-Type:text/html%s%s", 
                        crlf, 
                        crlf);

                /* reject !!! */
                pfc->ServerSupportFunction(pfc, 
                                           SF_REQ_SEND_RESPONSE_HEADER,
                                           "403 Forbidden",
                                           (DWORD)crlf,
                                           (DWORD)ctype);
                pfc->WriteClient(pfc, msg, &len, 0);

                return SF_STATUS_REQ_FINISHED;
            }
        }
    }
    return SF_STATUS_REQ_NEXT_NOTIFICATION;
}


BOOL WINAPI GetExtensionVersion(HSE_VERSION_INFO  *pVer)
{
    pVer->dwExtensionVersion = MAKELONG( HSE_VERSION_MINOR,
                                         HSE_VERSION_MAJOR );

    strcpy(pVer->lpszExtensionDesc, VERSION_STRING);


    if(!is_inited) {
        return initialize_extension();
    }

    return TRUE;
}

DWORD WINAPI HttpExtensionProc(LPEXTENSION_CONTROL_BLOCK  lpEcb)
{   
    DWORD rc = HSE_STATUS_ERROR;

    lpEcb->dwHttpStatusCode = HTTP_STATUS_SERVER_ERROR;

    jk_log(logger, JK_LOG_DEBUG, 
           "HttpExtensionProc started\n");

    if(is_inited) {
        isapi_private_data_t private_data;
        jk_ws_service_t s;
        jk_pool_atom_t buf[SMALL_POOL_SIZE];
        char *worker_name;

        jk_init_ws_service(&s);
        jk_open_pool(&private_data.p, buf, sizeof(buf));

        private_data.request_started = JK_FALSE;
        private_data.bytes_read_so_far = 0;
        private_data.lpEcb = lpEcb;

        s.ws_private = &private_data;
        s.pool = &private_data.p;

        if(init_ws_service(&private_data, &s, &worker_name)) {
            jk_worker_t *worker = wc_get_worker_for_name(worker_name, logger);

            jk_log(logger, JK_LOG_DEBUG, 
                   "HttpExtensionProc %s a worker for name %s\n", 
                   worker ? "got" : "could not get",
                   worker_name);

            if(worker) {
                jk_endpoint_t *e = NULL;
                if(worker->get_endpoint(worker, &e, logger)) {
                    int recover = JK_FALSE;
                    if(e->service(e, &s, logger, &recover)) {
                        rc = HSE_STATUS_SUCCESS_AND_KEEP_CONN;
                        lpEcb->dwHttpStatusCode = HTTP_STATUS_OK;
                        jk_log(logger, JK_LOG_DEBUG, 
                               "HttpExtensionProc service() returned OK\n");
                    } else {
                        jk_log(logger, JK_LOG_ERROR, 
                               "HttpExtensionProc error, service() failed\n");
                    }
                    e->done(&e, logger);
                }
            } else {
                jk_log(logger, JK_LOG_ERROR, 
                       "HttpExtensionProc error, could not get a worker for name %s\n",
                       worker_name);
            }
        }
        jk_close_pool(&private_data.p);     
    } else {
        jk_log(logger, JK_LOG_ERROR, 
               "HttpExtensionProc error, not initialized\n");
    }

    return rc;
}

    

BOOL WINAPI TerminateExtension(DWORD dwFlags) 
{
    return TerminateFilter(dwFlags);
}

BOOL WINAPI TerminateFilter(DWORD dwFlags) 
{
    if(is_inited) {
        is_inited = JK_FALSE;

        uri_worker_map_free(&uw_map, logger);
        wc_close(logger);
        if(logger) {
            jk_close_file_logger(&logger);
        }
    }
    
    return TRUE;
}


BOOL WINAPI DllMain(HINSTANCE hInst,        // Instance Handle of the DLL
                    ULONG ulReason,         // Reason why NT called this DLL
                    LPVOID lpReserved)      // Reserved parameter for future use
{
    BOOL fReturn = TRUE;
    char drive[_MAX_DRIVE];
    char dir[_MAX_DIR];
    char fname[_MAX_FNAME];
    char file_name[_MAX_PATH];

    switch (ulReason) {
        case DLL_PROCESS_DETACH:
            __try {
                TerminateFilter(HSE_TERM_MUST_UNLOAD);
            } __except(1) {
            }
        break;

        default:
        break;
    } 
    if (GetModuleFileName( hInst, file_name, sizeof(file_name))) {
        _splitpath( file_name, drive, dir, fname, NULL );
        _makepath( ini_file_name, drive, dir, fname, ".properties" );
    } else {
        fReturn = JK_FALSE;
    }

    return fReturn;
}

static int initialize_extension(void)
{
    int rc = JK_FALSE;  

    if(read_registry_init_data()) {
        jk_map_t *map;

        if(!jk_open_file_logger(&logger, log_file, log_level)) {
            logger = NULL;
        }

		/* Logging the initialization type: registry or properties file in virtual dir
		*/
		if (using_ini_file) {
			 jk_log(logger, JK_LOG_DEBUG, "Using ini file %s.\n", ini_file_name);
		} else {
			 jk_log(logger, JK_LOG_DEBUG, "Using registry.\n");
		}
		jk_log(logger, JK_LOG_DEBUG, "Using log file %s.\n", log_file);
		jk_log(logger, JK_LOG_DEBUG, "Using log level %d.\n", log_level);
		jk_log(logger, JK_LOG_DEBUG, "Using extension uri %s.\n", extension_uri);
		jk_log(logger, JK_LOG_DEBUG, "Using worker file %s.\n", worker_file);
		jk_log(logger, JK_LOG_DEBUG, "Using worker mount file %s.\n", worker_mount_file);

        
        if(map_alloc(&map)) {
            if(map_read_properties(map, worker_mount_file)) {
                if(uri_worker_map_alloc(&uw_map, map, logger)) {
                    rc = JK_TRUE;
                }
            }
            map_free(&map);
        }

        if(rc) {
            rc = JK_FALSE;
            if(map_alloc(&map)) {
                if(map_read_properties(map, worker_file)) {
                    if(wc_open(map, logger)) {
                        rc = JK_TRUE;
                    }
                }
                map_free(&map);
            }
        }

    }

    if(rc) {
        is_inited = JK_TRUE;
    }
    return rc;
}

static int read_registry_init_data(void)
{
    char tmpbuf[INTERNET_MAX_URL_LENGTH];
    HKEY hkey;
    long rc;
    int  ok = JK_TRUE;
    char *tmp;
    jk_map_t *map;

    if (map_alloc(&map)) {
        if (map_read_properties(map, ini_file_name)) {
            using_ini_file = JK_TRUE;
		}
    }
    if (using_ini_file) {
        tmp = map_get_string(map, JK_LOG_FILE_TAG, NULL);
        if (tmp) {
            strcpy(log_file, tmp);
        } else {
            ok = JK_FALSE;
        }
        tmp = map_get_string(map, JK_LOG_LEVEL_TAG, NULL);
        if (tmp) {
            log_level = jk_parse_log_level(tmp);
        } else {
            ok = JK_FALSE;
        }
        tmp = map_get_string(map, EXTENSION_URI_TAG, NULL);
        if (tmp) {
            strcpy(extension_uri, tmp);
        } else {
            ok = JK_FALSE;
        }
        tmp = map_get_string(map, JK_WORKER_FILE_TAG, NULL);
        if (tmp) {
            strcpy(worker_file, tmp);
        } else {
            ok = JK_FALSE;
        }
        tmp = map_get_string(map, JK_MOUNT_FILE_TAG, NULL);
        if (tmp) {
            strcpy(worker_mount_file, tmp);
        } else {
            ok = JK_FALSE;
        }
    
    } else {
		rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE,
						  REGISTRY_LOCATION,
						  (DWORD)0,         
						  KEY_READ,         
						  &hkey);            
		if(ERROR_SUCCESS != rc) {
			return JK_FALSE;
		} 

		if(get_registry_config_parameter(hkey,
										 JK_LOG_FILE_TAG, 
										 tmpbuf,
										 sizeof(log_file))) {
			strcpy(log_file, tmpbuf);
		} else {
			ok = JK_FALSE;
		}
    
		if(get_registry_config_parameter(hkey,
										 JK_LOG_LEVEL_TAG, 
										 tmpbuf,
										 sizeof(tmpbuf))) {
			log_level = jk_parse_log_level(tmpbuf);
		} else {
			ok = JK_FALSE;
		}

		if(get_registry_config_parameter(hkey,
										 EXTENSION_URI_TAG, 
										 tmpbuf,
										 sizeof(extension_uri))) {
			strcpy(extension_uri, tmpbuf);
		} else {
			ok = JK_FALSE;
		}

		if(get_registry_config_parameter(hkey,
										 JK_WORKER_FILE_TAG, 
										 tmpbuf,
										 sizeof(worker_file))) {
			strcpy(worker_file, tmpbuf);
		} else {
			ok = JK_FALSE;
		}

		if(get_registry_config_parameter(hkey,
										 JK_MOUNT_FILE_TAG, 
										 tmpbuf,
										 sizeof(worker_mount_file))) {
			strcpy(worker_mount_file, tmpbuf);
		} else {
			ok = JK_FALSE;
		}

		RegCloseKey(hkey);
    }    
    return ok;
}

static int get_registry_config_parameter(HKEY hkey,
                                         const char *tag, 
                                         char *b,
                                         DWORD sz)
{   
    DWORD type = 0;
    LONG  lrc;

    lrc = RegQueryValueEx(hkey,     
                          tag,      
                          (LPDWORD)0,
                          &type,    
                          (LPBYTE)b,
                          &sz); 
    if((ERROR_SUCCESS != lrc) || (type != REG_SZ)) {
        return JK_FALSE;        
    }
    
    b[sz] = '\0';

    return JK_TRUE;     
}

static int init_ws_service(isapi_private_data_t *private_data,
                           jk_ws_service_t *s,
                           char **worker_name) 
{
    char huge_buf[16 * 1024]; /* should be enough for all */

    DWORD huge_buf_sz;

    s->jvm_route = NULL;

    s->start_response = start_response;
    s->read = read;
    s->write = write;

    GET_SERVER_VARIABLE_VALUE(HTTP_WORKER_HEADER_NAME, (*worker_name));           
    GET_SERVER_VARIABLE_VALUE(HTTP_URI_HEADER_NAME, s->req_uri);     

    if(s->req_uri) {
        char *t = strchr(s->req_uri, '?');
        if(t) {
            *t = '\0';
            t++;
            if(!strlen(t)) {
                t = NULL;
            }
        }
        s->query_string = t;
    } else {
        s->query_string = private_data->lpEcb->lpszQueryString;
        *worker_name    = JK_AJP12_WORKER_NAME;
        GET_SERVER_VARIABLE_VALUE("URL", s->req_uri);       
    }
    
    GET_SERVER_VARIABLE_VALUE("AUTH_TYPE", s->auth_type);
    GET_SERVER_VARIABLE_VALUE("REMOTE_USER", s->remote_user);
    GET_SERVER_VARIABLE_VALUE("SERVER_PROTOCOL", s->protocol);
    GET_SERVER_VARIABLE_VALUE("REMOTE_HOST", s->remote_host);
    GET_SERVER_VARIABLE_VALUE("REMOTE_ADDR", s->remote_addr);
    GET_SERVER_VARIABLE_VALUE("SERVER_NAME", s->server_name);
    GET_SERVER_VARIABLE_VALUE_INT("SERVER_PORT", s->server_port, 80)
    GET_SERVER_VARIABLE_VALUE("SERVER_SOFTWARE", s->server_software);
    GET_SERVER_VARIABLE_VALUE_INT("SERVER_PORT_SECURE", s->is_ssl, 0);

    s->method           = private_data->lpEcb->lpszMethod;
    s->content_length   = private_data->lpEcb->cbTotalBytes;

    s->ssl_cert     = NULL;
    s->ssl_cert_len = 0;
    s->ssl_cipher   = NULL;
    s->ssl_session  = NULL;

    s->headers_names    = NULL;
    s->headers_values   = NULL;
    s->num_headers      = 0;
    
    /*
     * Add SSL IIS environment
     */
    if(s->is_ssl) {         
        char *ssl_env_names[9] = {
            "CERT_ISSUER", 
            "CERT_SUBJECT", 
            "CERT_COOKIE", 
            "HTTPS_SERVER_SUBJECT", 
            "CERT_FLAGS", 
            "HTTPS_SECRETKEYSIZE", 
            "CERT_SERIALNUMBER", 
            "HTTPS_SERVER_ISSUER", 
            "HTTPS_KEYSIZE"
        };
        char *ssl_env_values[9] = {
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL, 
            NULL
        };
        unsigned i;
        unsigned num_of_vars = 0;

        for(i = 0 ; i < 9 ; i++) {
            GET_SERVER_VARIABLE_VALUE(ssl_env_names[i], ssl_env_values[i]);
            if(ssl_env_values[i]) {
				jk_log(logger, JK_LOG_DEBUG, "SSL vars %s:%s.\n", ssl_env_names[i], ssl_env_values[i]);
                num_of_vars++;
            }
        }
        if(num_of_vars) {
            unsigned j;

            s->attributes_names = 
                jk_pool_alloc(&private_data->p, num_of_vars * sizeof(char *));
            s->attributes_values = 
                jk_pool_alloc(&private_data->p, num_of_vars * sizeof(char *));

            j = 0;
            for(i = 0 ; i < 9 ; i++) {                
                if(ssl_env_values[i]) {
                    s->attributes_names[j] = ssl_env_names[i];
                    s->attributes_values[j] = ssl_env_values[i];
                    j++;
                }
            }
            s->num_attributes = num_of_vars;
 			if (ssl_env_values[4] && ssl_env_values[4][0] == '1') {
				CERT_CONTEXT_EX cc;
				DWORD cc_sz = sizeof(cc);
				cc.cbAllocated = sizeof(huge_buf);
				cc.CertContext.pbCertEncoded = (BYTE*) huge_buf;
				cc.CertContext.cbCertEncoded = 0;

				if (private_data->lpEcb->ServerSupportFunction(private_data->lpEcb->ConnID,
											 (DWORD)HSE_REQ_GET_CERT_INFO_EX,                               
											 (LPVOID)&cc,NULL,NULL) != FALSE)
				{
					jk_log(logger, JK_LOG_DEBUG,"Client Certificate encoding:%d sz:%d flags:%ld\n",
								cc.CertContext.dwCertEncodingType & X509_ASN_ENCODING ,
								cc.CertContext.cbCertEncoded,
								cc.dwCertificateFlags);
                    s->ssl_cert=jk_pool_alloc(&private_data->p,
                                base64_encode_cert_len(cc.CertContext.cbCertEncoded));

                    s->ssl_cert_len = base64_encode_cert(s->ssl_cert,
                                huge_buf,cc.CertContext.cbCertEncoded) - 1;
				}
			}
        }
    }

    huge_buf_sz = sizeof(huge_buf);         
    if(get_server_value(private_data->lpEcb,
                        "ALL_HTTP",             
                        huge_buf,           
                        huge_buf_sz,        
                        "")) {              
        unsigned cnt = 0;
        char *tmp;

        for(tmp = huge_buf ; *tmp ; tmp++) {
            if(*tmp == '\n'){
                cnt++;
            }
        }

        if(cnt) {
            char *headers_buf = jk_pool_strdup(&private_data->p, huge_buf);
            unsigned i;
            unsigned len_of_http_prefix = strlen("HTTP_");
            BOOL need_content_length_header = (s->content_length == 0);
            
            cnt -= 2; /* For our two special headers */
            /* allocate an extra header slot in case we need to add a content-length header */
            s->headers_names  = jk_pool_alloc(&private_data->p, (cnt + 1) * sizeof(char *));
            s->headers_values = jk_pool_alloc(&private_data->p, (cnt + 1) * sizeof(char *));

            if(!s->headers_names || !s->headers_values || !headers_buf) {
                return JK_FALSE;
            }

            for(i = 0, tmp = headers_buf ; *tmp && i < cnt ; ) {
                int real_header = JK_TRUE;

                /* Skipp the HTTP_ prefix to the beginning of th header name */
                tmp += len_of_http_prefix;

                if(!strnicmp(tmp, URI_HEADER_NAME, strlen(URI_HEADER_NAME)) ||
                   !strnicmp(tmp, WORKER_HEADER_NAME, strlen(WORKER_HEADER_NAME))) {
                    real_header = JK_FALSE;
                } else if(need_content_length_header &&
                   !strnicmp(tmp, CONTENT_LENGTH, strlen(CONTENT_LENGTH))) {
                    need_content_length_header = FALSE;
                } else {
                    s->headers_names[i]  = tmp;
                }

                while(':' != *tmp && *tmp) {
                    if('_' == *tmp) {
                        *tmp = '-';
                    } else {
                        *tmp = tolower(*tmp);
                    }
                    tmp++;
                }
                *tmp = '\0';
                tmp++;

                /* Skipp all the WS chars after the ':' to the beginning of th header value */
                while(' ' == *tmp || '\t' == *tmp || '\v' == *tmp) {
                    tmp++;
                }

                if(real_header) {
                    s->headers_values[i]  = tmp;
                }
                
                while(*tmp != '\n' && *tmp != '\r') {
                    tmp++;
                }
                *tmp = '\0';
                tmp++;

                /* skipp CR LF */
                while(*tmp == '\n' || *tmp == '\r') {
                    tmp++;
                }

                if(real_header) {
                    i++;
                }
            }
            /* Add a content-length = 0 header if needed.
             * Ajp13 assumes an absent content-length header means an unknown,
             * but non-zero length body.
             */
            if(need_content_length_header) {
                s->headers_names[cnt] = "content-length";
                s->headers_values[cnt] = "0";
                cnt++;
            }
            s->num_headers = cnt;
        } else {
            /* We must have our two headers */
            return JK_FALSE;
        }
    } else {
        return JK_FALSE;
    }
    
    return JK_TRUE;
}

static int get_server_value(LPEXTENSION_CONTROL_BLOCK lpEcb,
                            char *name,
                            char  *buf,
                            DWORD bufsz,
                            char  *def_val)
{
    if(!lpEcb->GetServerVariable(lpEcb->ConnID, 
                                 name,
                                 buf,
                                 (LPDWORD)&bufsz)) {
        strcpy(buf, def_val);
        return JK_FALSE;
    }

    if(bufsz > 0) {
        buf[bufsz - 1] = '\0';
    }

    return JK_TRUE;
}

static const char begin_cert [] = 
	"-----BEGIN CERTIFICATE-----\r\n";

static const char end_cert [] = 
	"-----END CERTIFICATE-----\r\n";

static const char basis_64[] =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static int base64_encode_cert_len(int len)
{
	int n = ((len + 2) / 3 * 4) + 1; // base64 encoded size
	n += (n + 63 / 64) * 2; // add CRLF's
	n += sizeof(begin_cert) + sizeof(end_cert) - 2;  // add enclosing strings.
    return n;
}

static int base64_encode_cert(char *encoded,
                              const unsigned char *string, int len)
{
    int i,c;
    char *p;
	const char *t;

    p = encoded;

	t = begin_cert;
	while (*t != '\0')
		*p++ = *t++;

    c = 0;
    for (i = 0; i < len - 2; i += 3) {
        *p++ = basis_64[(string[i] >> 2) & 0x3F];
        *p++ = basis_64[((string[i] & 0x3) << 4) |
                        ((int) (string[i + 1] & 0xF0) >> 4)];
        *p++ = basis_64[((string[i + 1] & 0xF) << 2) |
                        ((int) (string[i + 2] & 0xC0) >> 6)];
        *p++ = basis_64[string[i + 2] & 0x3F];
        c += 4;
        if ( c >= 64 ) {
            *p++ = '\r';
            *p++ = '\n';
            c = 0;
		}
    }
    if (i < len) {
        *p++ = basis_64[(string[i] >> 2) & 0x3F];
        if (i == (len - 1)) {
            *p++ = basis_64[((string[i] & 0x3) << 4)];
            *p++ = '=';
        }
        else {
            *p++ = basis_64[((string[i] & 0x3) << 4) |
                            ((int) (string[i + 1] & 0xF0) >> 4)];
            *p++ = basis_64[((string[i + 1] & 0xF) << 2)];
        }
        *p++ = '=';
        c++;
    }
    if ( c != 0 ) {
        *p++ = '\r';
        *p++ = '\n';
    }

	t = end_cert;
	while (*t != '\0')
		*p++ = *t++;

    *p++ = '\0';
    return p - encoded;
}
