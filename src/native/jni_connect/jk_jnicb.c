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
 * Description: JNI callbacks implementation for the JNI in process adapter*
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.2 $                                               *
 ***************************************************************************/

#include "jk_jnicb.h"
#include "jk_service.h"
#include "jk_util.h"
#include "jk_pool.h"

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    getNumberOfHeaders
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_getNumberOfHeaders
  (JNIEnv *env, jobject o, jlong s, jlong l)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;

    if(ps) {
        return (jint)ps->num_headers;
    }

    jk_log(pl, JK_LOG_ERROR, 
           "In JNIConnectionHandler::getNumberOfHeaders, NULL ws service object\n");
    return -1;
}

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    read
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_read
  (JNIEnv *env, jobject o, jlong s, jlong l, jbyteArray buf, jint from, jint cnt)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;
    jint rc = -1;

    if(ps) {
        jboolean iscommit;
        jbyte *nbuf = (*env)->GetByteArrayElements(env, buf, &iscommit);
        unsigned nfrom = (unsigned)from;
        unsigned ncnt = (unsigned)cnt;
        unsigned acc = 0;

        if(nbuf) {
            if(!ps->read(ps, nbuf + nfrom, ncnt, &acc)) {
                jk_log(pl, JK_LOG_ERROR, 
                       "In JNIConnectionHandler::read, failed to read from the web server\n");
            } else {
                rc = (jint)acc;
            }                
            (*env)->ReleaseByteArrayElements(env, buf, nbuf, 0);
        }
    } else {
        jk_log(pl, JK_LOG_ERROR, 
               "In JNIConnectionHandler::read, NULL ws service object\n");
    }
    return rc;
}

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    readEnvironment
 * Signature: (JJ[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_readEnvironment
  (JNIEnv *env, jobject o, jlong s, jlong l, jobjectArray envbuf)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;

    if(ps) {
        char port[10];

        sprintf(port, "%d", ps->server_port);
        
        if(ps->method) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          0, 
                                          (*env)->NewStringUTF(env, ps->method));
        }
        if(ps->req_uri) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          1, 
                                          (*env)->NewStringUTF(env, ps->req_uri));
        }
        if(ps->query_string) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          2, 
                                          (*env)->NewStringUTF(env, ps->query_string));
        }

        if(ps->remote_addr) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          3, 
                                          (*env)->NewStringUTF(env, ps->remote_addr));
        }

        if(ps->remote_host) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          4, 
                                          (*env)->NewStringUTF(env, ps->remote_host));
        }

        if(ps->server_name) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          5, 
                                          (*env)->NewStringUTF(env, ps->server_name));
        }

        (*env)->SetObjectArrayElement(env, 
                                      envbuf, 
                                      6, 
                                      (*env)->NewStringUTF(env, port));

        if(ps->auth_type) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          7, 
                                          (*env)->NewStringUTF(env, ps->auth_type));
        }
        if(ps->remote_user) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          8, 
                                          (*env)->NewStringUTF(env, ps->remote_user));
        }
        if(ps->is_ssl) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          9, 
                                          (*env)->NewStringUTF(env, "https"));
        } else {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          9, 
                                          (*env)->NewStringUTF(env, "http"));
        }

        if(ps->protocol) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          10, 
                                          (*env)->NewStringUTF(env, ps->protocol));
        }
        if(ps->server_software) {
            (*env)->SetObjectArrayElement(env, 
                                          envbuf, 
                                          11, 
                                          (*env)->NewStringUTF(env, ps->server_software));
        }
        
        return JK_TRUE;
    } else {
        jk_log(pl, JK_LOG_ERROR, 
               "In JNIConnectionHandler::readEnvironment, NULL ws service object\n");
    }

    return JK_FALSE;
}

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    readHeaders
 * Signature: (JJ[Ljava/lang/String;[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_readHeaders
  (JNIEnv *env, jobject o, jlong s, jlong l, jobjectArray hnames, jobjectArray hvalues)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;

    if(ps) {
        unsigned i;

        for(i = 0 ; i < ps->num_headers ; i++) {
            (*env)->SetObjectArrayElement(env, 
                                          hnames, 
                                          i, 
                                          (*env)->NewStringUTF(env, ps->headers_names[i]));
            (*env)->SetObjectArrayElement(env, 
                                          hvalues, 
                                          i, 
                                          (*env)->NewStringUTF(env, ps->headers_values[i]));
        }

	return JK_TRUE;
    } else {
        jk_log(pl, JK_LOG_ERROR, 
               "In JNIConnectionHandler::readHeaders, NULL ws service object\n");
    }

    return JK_FALSE;
}

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    startReasponse
 * Signature: (JJILjava/lang/String;[Ljava/lang/String;[Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_startReasponse
  (JNIEnv *env, jobject o, jlong s, jlong l, 
   jint sc, jstring msg, jobjectArray hnames, jobjectArray hvalues, jint hcnt)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;
    
    if(ps) {
        const char *nmsg = NULL;
        char **nhnames = NULL;
        char **nhvalues = NULL;
        jstring *shnames = NULL;
        jstring *shvalues = NULL;
        int i = 0;
        int ok = JK_TRUE;

        if(hcnt) {
            ok = JK_FALSE;

            nhnames = (char **)jk_pool_alloc(ps->pool, hcnt * sizeof(char *));
            nhvalues = (char **)jk_pool_alloc(ps->pool, hcnt * sizeof(char *));            
            shnames = (jstring *)jk_pool_alloc(ps->pool, hcnt * sizeof(jstring));
            shvalues = (jstring *)jk_pool_alloc(ps->pool, hcnt * sizeof(jstring));
            
            if(nhvalues && nhnames && shnames && shnames) {
                for( ; i < hcnt ; i++) {
                    jboolean iscommit;

                    shvalues[i] = shnames[i] = NULL;
                    nhnames[i] = nhvalues[i] = NULL;

                    shnames[i] = (*env)->GetObjectArrayElement(env, hnames, i);
                    shvalues[i] = (*env)->GetObjectArrayElement(env, hvalues, i);

                    if(!shvalues[i] || !shnames[i]) {
                        jk_log(pl, JK_LOG_ERROR, 
                               "In JNIConnectionHandler::startReasponse, GetObjectArrayElement error\n");
                        break;
                    }

                    nhnames[i] = (char *)(*env)->GetStringUTFChars(env, shnames[i], &iscommit);
                    nhvalues[i] = (char *)(*env)->GetStringUTFChars(env, shvalues[i], &iscommit);

                    if(!nhvalues[i] || !nhnames[i]) {
                        jk_log(pl, JK_LOG_ERROR, 
                               "In JNIConnectionHandler::startReasponse, GetStringUTFChars error\n");
                        break;
                    }
                }
                if(i == hcnt) {
                    ok = JK_TRUE;
                }
            }
        } else {
            jk_log(pl, JK_LOG_ERROR, 
                   "In JNIConnectionHandler::startReasponse, memory allocation error\n");
        }
        
        if(msg && ok) {
            jboolean iscommit;
            nmsg = (*env)->GetStringUTFChars(env, msg, &iscommit);
            if(!nmsg) {
                ok = JK_FALSE;
            }
        }

        if(ok) {
            if(!ps->start_response(ps, sc, nmsg, nhnames, nhvalues, hcnt)) {
                ok = JK_FALSE;
            } else {
                jk_log(pl, JK_LOG_ERROR, 
                       "In JNIConnectionHandler::startReasponse, servers startReasponse failed\n");
            }
        }

        if(nmsg) {
            (*env)->ReleaseStringUTFChars(env, msg, nmsg);
        }
        
        if(i < hcnt) {
            i++;
        }

        if(nhvalues) {
            int j;

            for(j = 0 ; j < i ; j++) {
                if(nhvalues[j]) {
                    (*env)->ReleaseStringUTFChars(env, shvalues[j], nhvalues[j]);
                }
            }
        }

        if(nhnames) {
            int j;

            for(j = 0 ; j < i ; j++) {
                if(nhnames[j]) {
                    (*env)->ReleaseStringUTFChars(env, shnames[j], nhnames[j]);
                }
            }
        }

        return ok;
    } else {
        jk_log(pl, JK_LOG_ERROR, 
               "In JNIConnectionHandler::startReasponse, NULL ws service object\n");
    }

    return JK_FALSE;
}

/*
 * Class:     org_apache_tomcat_service_connector_JNIConnectionHandler
 * Method:    write
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_tomcat_service_connector_JNIConnectionHandler_write
  (JNIEnv *env, jobject o, jlong s, jlong l, jbyteArray buf, jint from, jint cnt)
{
    jk_ws_service_t *ps = (jk_ws_service_t *)s;
    jk_logger_t *pl = (jk_logger_t *)l;
    jint rc = JK_FALSE;   

    if(ps) {
        jboolean iscommit;
        jbyte *nbuf = (*env)->GetByteArrayElements(env, buf, &iscommit);
        unsigned nfrom = (unsigned)from;
        unsigned ncnt = (unsigned)cnt;

        if(nbuf) {
            if(!ps->write(ps, nbuf + nfrom, ncnt)) {
                jk_log(pl, JK_LOG_ERROR, 
                       "In JNIConnectionHandler::write, failed to write to the web server\n");
            } else {
                rc = (jint)JK_TRUE;
            }                
            (*env)->ReleaseByteArrayElements(env, buf, nbuf, JNI_ABORT);
        } else {
            jk_log(pl, JK_LOG_ERROR, 
                   "In JNIConnectionHandler::write, GetByteArrayElements error\n");
        }
    } else {
        jk_log(pl, JK_LOG_ERROR, 
               "In JNIConnectionHandler::write, NULL ws service object\n");
    }

    return rc; 
}
