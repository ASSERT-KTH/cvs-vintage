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
 * Description: In process JNI worker                                      *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Based on:                                                               *
 * Version:     $Revision: 1.3 $                                               *
 ***************************************************************************/

#include <jni.h>

#include "jk_pool.h"
#include "jk_jni_worker.h"
#include "jk_util.h"


jint (JNICALL *jni_get_default_java_vm_init_args)(void *) = NULL;
jint (JNICALL *jni_create_java_vm)(JavaVM **, JNIEnv **, void *) = NULL;

#define JAVA_BRIDGE_CLASS_NAME ("org/apache/tomcat/service/JNIEndpoint")
 
static int jni_instance_created = JK_FALSE;

struct jni_worker {

    jk_pool_t p;
    jk_pool_atom_t buf[TINY_POOL_SIZE];
    /*
     * JVM Object pointer.
     */
    JavaVM      *jvm;   

    /*
     * Web Server to Java bridge, instance and class.
     */
    jobject     jk_java_bridge_object;
    jclass      jk_java_bridge_class;

    /*
     * Java methods ids, to jump into the JVM
     */
    jmethodID   jk_startup_method;
    jmethodID   jk_service_method;
    jmethodID   jk_shutdown_method;

    /*
     * Command line for tomcat startup
     */
    char *tomcat_cmd_line;

    /*
     * Classpath
     */
    char *tomcat_classpath;

    /*
     * Full path to the jni javai/jvm dll
     */
    char *jvm_dll_path;

    /*
     * Initial Java heap size
     */
    unsigned tomcat_ms;

    /*
     * Max Java heap size
     */
    unsigned tomcat_mx;

    /*
     * Javas system properties.
     */
    char **sysprops;

    /*
     * stdout and stderr file names for Java
     */
    char *stdout_name;
    char *stderr_name;

    char *name; 
    jk_worker_t worker;
};
typedef struct jni_worker jni_worker_t;

struct jni_endpoint {    
    int attached;
    JNIEnv *env;
    jni_worker_t *worker;
    
    jk_endpoint_t endpoint;
};
typedef struct jni_endpoint jni_endpoint_t;


static int load_jvm_dll(jni_worker_t *p,
                        jk_logger_t *l);

static int open_jvm(jni_worker_t *p,
                    JNIEnv **env,
                    jk_logger_t *l);

static int get_bridge_object(jni_worker_t *p,
                             JNIEnv *env,
                             jk_logger_t *l);

static int get_method_ids(jni_worker_t *p,
                          JNIEnv *env,
                          jk_logger_t *l);

static JNIEnv *attach_to_jvm(jni_worker_t *p);

static void detach_from_jvm(jni_worker_t *p);

static void append_libpath(jk_pool_t *p, 
                           const char *libpath);
  
static int JK_METHOD service(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l,
                             int *is_recoverable_error)
{
    if(e && e->endpoint_private && s && is_recoverable_error) {
        jni_endpoint_t *p = e->endpoint_private;

        if(p->attached ||
           (p->env = attach_to_jvm(p->worker))) {
            jint rc = 0;

            p->attached = JK_TRUE;

            /* 
             * When we call the JVM we can not know what happen 
             * So we can not recover !!!
             */
            *is_recoverable_error = JK_FALSE;

            rc = (*(p->env))->CallIntMethod(p->env,
                                            p->worker->jk_java_bridge_object,
                                            p->worker->jk_service_method,
                                            (jlong)s,
                                            (jlong)l);

            return rc == 0 ? JK_FALSE : JK_TRUE;
        }
        *is_recoverable_error = JK_TRUE;
        
    }
    return JK_FALSE;
}

static int JK_METHOD done(jk_endpoint_t **e,
                          jk_logger_t *l)
{
    if(e && *e && (*e)->endpoint_private) {
        jni_endpoint_t *p = (*e)->endpoint_private;

        if(p->attached) {
            detach_from_jvm(p->worker);
        }

        free(p);
        *e = NULL;
        return JK_TRUE;
    }

    return JK_FALSE;
}

static int JK_METHOD validate(jk_worker_t *pThis,
                              jk_map_t *props,
                              jk_logger_t *l)
{
    if(pThis && pThis->worker_private) {        
        jni_worker_t *p = pThis->worker_private;
        unsigned mem_config = 0;
        char *str_config = NULL;
        JNIEnv *env;

        if(jk_get_worker_mx(props, p->name, &mem_config)) {
            p->tomcat_mx = mem_config;
        }

        if(jk_get_worker_ms(props, p->name, &mem_config)) {
            p->tomcat_ms = mem_config;
        }

        if(jk_get_worker_classpath(props, p->name, &str_config)) {
            p->tomcat_classpath = jk_pool_strdup(&p->p, str_config);
        }

        if(!p->tomcat_classpath) {
            return JK_FALSE;
        }

        if(jk_get_worker_jvm_path(props, p->name, &str_config)) {
            p->jvm_dll_path  = jk_pool_strdup(&p->p, str_config);
        }

        if(!p->jvm_dll_path || 
            !jk_file_exists(p->jvm_dll_path)) {
            return JK_FALSE;
        }

        if(jk_get_worker_cmd_line(props, p->name, &str_config)) {
            p->tomcat_cmd_line  = jk_pool_strdup(&p->p, str_config);
        }

        if(jk_get_worker_stdout(props, p->name, &str_config)) {
            p->stdout_name  = jk_pool_strdup(&p->p, str_config);
        }

        if(jk_get_worker_stderr(props, p->name, &str_config)) {
            p->stderr_name  = jk_pool_strdup(&p->p, str_config);
        }

        if(jk_get_worker_sysprops(props, p->name, &str_config)) {
            p->sysprops  = jk_parse_sysprops(&p->p, str_config);
        }
        
        if(jk_get_worker_libpath(props, p->name, &str_config)) {
            append_libpath(&p->p, str_config);
        }


        if(load_jvm_dll(p, l)) {
            if(open_jvm(p, &env, l)) {
                if(get_bridge_object(p, env, l)) {
                    if(get_method_ids(p, env, l)) {
                        return JK_TRUE;
                    }
                }
            }            
        }

        if(p->jvm) {
            detach_from_jvm(p);
        }
    }

    return JK_FALSE;
}

static int JK_METHOD init(jk_worker_t *pThis,
                          jk_map_t *props, 
                          jk_logger_t *log)
{
    if(pThis && pThis->worker_private) {        
        jni_worker_t *p = pThis->worker_private;
        JNIEnv *env;

        if(!p->jvm ||
           !p->jk_java_bridge_object ||
           !p->jk_service_method     ||
           !p->jk_startup_method     ||
           !p->jk_shutdown_method) {
            return JK_FALSE;
        }
       
        if(env = attach_to_jvm(p)) {
            jstring cmd_line = NULL;
            jstring stdout_name = NULL;
            jstring stderr_name = NULL;
            jint rc = 0;
            
            if(p->tomcat_cmd_line) {
                cmd_line = (*env)->NewStringUTF(env, p->tomcat_cmd_line); 
            }
            if(p->stdout_name) {
                stdout_name = (*env)->NewStringUTF(env, p->stdout_name); 
            }
            if(p->stdout_name) {
                stderr_name = (*env)->NewStringUTF(env, p->stderr_name); 
            }

            rc = (*env)->CallIntMethod(env,
                                       p->jk_java_bridge_object, 
                                       p->jk_startup_method,
                                       cmd_line,
                                       stdout_name,
                                       stderr_name);

            detach_from_jvm(p);
            return rc == 0 ? JK_FALSE : JK_TRUE;
        }
    }
    return JK_FALSE;
}

static int JK_METHOD get_endpoint(jk_worker_t *pThis,
                                  jk_endpoint_t **pend,
                                  jk_logger_t *log)
{
    if(pThis && pThis->worker_private && pend) {        
        jni_endpoint_t *p = (jni_endpoint_t *)malloc(sizeof(jni_endpoint_t));
        if(p) {

            p->attached = JK_FALSE;
            p->env = NULL;
            p->worker = pThis->worker_private;
            p->endpoint.endpoint_private = p;
            p->endpoint.service = service;
            p->endpoint.done = done;
            *pend = &p->endpoint;

            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

static int JK_METHOD destroy(jk_worker_t **pThis,
                             jk_logger_t *l)
{
    if(pThis && *pThis && (*pThis)->worker_private) {
        jni_worker_t *p = (*pThis)->worker_private;

        if(p->jvm) {
            if(p->jk_java_bridge_object && p->jk_shutdown_method) {
                JNIEnv *env;

                if(env = attach_to_jvm(p)) {
                    (*env)->CallVoidMethod(env,
                                           p->jk_java_bridge_object, 
                                           p->jk_shutdown_method);
                    detach_from_jvm(p);
                }                
            }

            /* 
             * FIXME:
             * Block forever ...remove for now
             *
            (*(p->jvm))->DestroyJavaVM(p->jvm);
             */
        }

        jk_close_pool(&p->p);
        free(p);

        return JK_TRUE;
    }

    return JK_FALSE;
}

int JK_METHOD jni_worker_factory(jk_worker_t **w,
                                 const char *name,
                                 jk_logger_t *l)
{

    if(NULL != name && NULL != w) {
        if(jni_instance_created) {
            *w = NULL;
            return JK_FALSE;
        } else {
            jni_worker_t *private_data = 
                (jni_worker_t *)malloc(sizeof(jni_worker_t ));

            if(private_data) {

                jk_open_pool(&private_data->p, 
                             private_data->buf, 
                             sizeof(jk_pool_atom_t) * TINY_POOL_SIZE);

                private_data->name = jk_pool_strdup(&private_data->p, name);          

                if(private_data->name) {
                    private_data->jvm                   = NULL; 
                    private_data->jk_java_bridge_object = NULL;
                    private_data->jk_java_bridge_class  = NULL;
                    private_data->jk_startup_method     = NULL;
                    private_data->jk_service_method     = NULL;
                    private_data->jk_shutdown_method    = NULL;
                    private_data->tomcat_cmd_line       = NULL;
                    private_data->tomcat_classpath      = NULL;
                    private_data->jvm_dll_path          = NULL;
                    private_data->tomcat_ms             = 0;
                    private_data->tomcat_mx             = 0;
                    private_data->sysprops              = NULL;
                    private_data->stdout_name           = NULL;
                    private_data->stderr_name           = NULL;

                    private_data->worker.worker_private = private_data;
                    private_data->worker.validate       = validate;
                    private_data->worker.init           = init;
                    private_data->worker.get_endpoint   = get_endpoint;
                    private_data->worker.destroy        = destroy;

                    *w = &private_data->worker;
                    return JK_TRUE;
                }

                jk_close_pool(&private_data->p);
                free(private_data);
            }
        }
    }

    return JK_FALSE;
}

static int load_jvm_dll(jni_worker_t *p,
                        jk_logger_t *l)
{
#ifdef WIN32
    HINSTANCE hInst = LoadLibrary(p->jvm_dll_path);
    if(hInst) {
        (FARPROC)jni_create_java_vm = 
            GetProcAddress(hInst, "JNI_CreateJavaVM");

        (FARPROC)jni_get_default_java_vm_init_args = 
            GetProcAddress(hInst, "JNI_GetDefaultJavaVMInitArgs");

        if(jni_create_java_vm && jni_get_default_java_vm_init_args) {
            return JK_TRUE;
        }

        FreeLibrary(hInst);
    }
#else 
    void *handle = dlopen(path, RTLD_NOW | RTLD_GLOBAL);
    if(handle) {
        jni_create_java_vm = dlsym(handle, "JNI_CreateJavaVM");
        jni_get_default_java_vm_init_args = dlsym(handle, "JNI_GetDefaultJavaVMInitArgs");
        if(jni_create_java_vm && jni_get_default_java_vm_init_args) {
            return JK_TRUE;
        }
        dlclose(handle);
    }
#endif

    return JK_FALSE;
}

static int open_jvm(jni_worker_t *p,
                    JNIEnv **env,
                    jk_logger_t *l)
{
    JDK1_1InitArgs vm_args;  
    JNIEnv *penv;
    jsize num_JavaVMs = 0;    
    *env = NULL;

    if(0 != jni_get_default_java_vm_init_args(&vm_args)) {
        return JK_FALSE;
    }

    vm_args.version = 0x00010001;

    if(vm_args.classpath) {
        unsigned len = strlen(vm_args.classpath) + 
                       strlen(p->tomcat_classpath) + 
                       3;
        char *tmp = jk_pool_alloc(&p->p, len);
        if(tmp) {
            sprintf(tmp, "%s%c%s", 
                    p->tomcat_classpath, 
                    PATH_SEPERATOR,
                    vm_args.classpath);
            p->tomcat_classpath = tmp;
        } else {
            return JK_FALSE;
        }
    }
    vm_args.classpath = p->tomcat_classpath;

    if(p->tomcat_mx) {
        vm_args.maxHeapSize = p->tomcat_mx;
    }

    if(p->tomcat_ms) {
        vm_args.minHeapSize = p->tomcat_ms;
    }

    if(p->sysprops) {
        vm_args.properties = p->sysprops;
    }

    if(jni_create_java_vm(&(p->jvm), 
                          &penv, 
                          &vm_args) != 0) {
        return JK_FALSE;
    }

    *env = penv;

    return JK_TRUE;
}

static int get_bridge_object(jni_worker_t *p,
                             JNIEnv *env,
                             jk_logger_t *l)
{
    char    *ctor_method_name     = "<init>";
    char    *ose_ctor_method_sig    = "()V";
    jmethodID  constructor_method_id = NULL;

    p->jk_java_bridge_class = (*env)->FindClass(env, JAVA_BRIDGE_CLASS_NAME);
    if(p->jk_java_bridge_class) {
        jmethodID  constructor_method_id = (*env)->GetMethodID(env,
                                                               p->jk_java_bridge_class, 
                                                               "<init>", /* method name */
                                                               "()V");   /* method sign */   
        if(constructor_method_id) {
            p->jk_java_bridge_object = (*env)->NewObject(env, 
                                                         p->jk_java_bridge_class,
                                                         constructor_method_id);
            if(p->jk_java_bridge_object) {
                p->jk_java_bridge_object = (jobject)(*env)->NewGlobalRef(env, p->jk_java_bridge_object);
                if(p->jk_java_bridge_object) {
                    return JK_TRUE;
                }
                p->jk_java_bridge_object = NULL;
            }
        }
        p->jk_java_bridge_class = NULL;
    }
    
    return JK_FALSE;
}

static int get_method_ids(jni_worker_t *p,
                          JNIEnv *env,
                          jk_logger_t *l)
{
    p->jk_startup_method = (*env)->GetMethodID(env,
                                               p->jk_java_bridge_class, 
                                               "startup", 
                                               "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I");  
    if(!p->jk_startup_method) {
        return JK_FALSE;
    }

    p->jk_service_method = (*env)->GetMethodID(env,
                                               p->jk_java_bridge_class, 
                                               "service", 
                                               "(JJ)I");   
    if(!p->jk_service_method) {
        return JK_FALSE;
    }

    p->jk_shutdown_method = (*env)->GetMethodID(env,
                                                p->jk_java_bridge_class, 
                                                "shutdown", 
                                                "()V");   
    if(!p->jk_shutdown_method) {
        return JK_FALSE;
    }    
    
    return JK_TRUE;
}


static JNIEnv *attach_to_jvm(jni_worker_t *p)
{
    JNIEnv *rc = NULL;
    if(0 == (*(p->jvm))->AttachCurrentThread(p->jvm, 
#ifdef JNI_VERSION_1_2 
           (void **)
#endif
                                             &rc, 
                                             NULL)) {
        return rc;
    }

    return NULL;
}

static void detach_from_jvm(jni_worker_t *p)
{
    (*(p->jvm))->DetachCurrentThread(p->jvm);
}

static void append_libpath(jk_pool_t *p, 
                           const char *libpath)
{
    char *env = NULL;
    char *current = getenv(PATH_ENV_VARIABLE);

    if(current) {
        env = jk_pool_alloc(p, strlen(PATH_ENV_VARIABLE) + 
                               strlen(current) + 
                               strlen(libpath) + 5);
        if(env) {
            sprintf(env, "%s=%s%c%s", 
                    PATH_ENV_VARIABLE, 
                    libpath, 
                    PATH_SEPERATOR, 
                    current);
        }
    } else {
        env = jk_pool_alloc(p, strlen(PATH_ENV_VARIABLE) +                               
                               strlen(libpath) + 5);
        if(env) {
            sprintf(env, "%s=%s", PATH_ENV_VARIABLE, libpath);
        }
    }

    if(env) {
        putenv(env);
    }
}