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
 * Version:     $Revision: 1.5 $                                               *
 ***************************************************************************/

#ifndef WIN32
#include <dlfcn.h>
#endif

#include <jni.h>

#include "jk_pool.h"
#include "jk_jni_worker.h"
#include "jk_util.h"

#ifdef LINUX
#include <pthread.h>
#include <signal.h>
#include <bits/signum.h>
#endif

jint (JNICALL *jni_get_default_java_vm_init_args)(void *) = NULL;
jint (JNICALL *jni_create_java_vm)(JavaVM **, JNIEnv **, void *) = NULL;

#define JAVA_BRIDGE_CLASS_NAME ("org/apache/tomcat/service/JNIEndpoint")
 
static jk_worker_t *the_singleton_jni_worker = NULL;

struct jni_worker {

    int was_verified;
    int was_initialized;

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

static int JK_METHOD service(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l,
                             int *is_recoverable_error)
{
    if( ! e ||  ! e->endpoint_private || ! s ) {
	jk_log(l, JK_LOG_EMERG, "Assert failed - invalid parameters\n");  
	return JK_FALSE;
    }
    
    if( ! is_recoverable_error ) {
	return JK_FALSE;
    }

    {
	jni_endpoint_t *p = e->endpoint_private;

        if(! p->attached ) { 
	    /* Try to attach */
	    if( ! (p->env = attach_to_jvm(p->worker))) {
		jk_log(l, JK_LOG_EMERG, "Attach failed\n");  
		/*   Is it recoverable ?? */
		*is_recoverable_error = JK_TRUE;
		return JK_FALSE;
	    } 
            p->attached = JK_TRUE;
	}

	/* we are attached now */
	{ 
	    jint rc = 0;
            /* 
             * When we call the JVM we can not know what happen 
             * So we can not recover !!!
             */
            *is_recoverable_error = JK_FALSE;

	    jk_log(l, JK_LOG_DEBUG, "Calling native method\n");  

            rc = (*(p->env))->CallIntMethod(p->env,
                                            p->worker->jk_java_bridge_object,
                                            p->worker->jk_service_method,
                                            (jlong)s,
                                            (jlong)l );

	    if( rc==0 ) {
		jk_log(l, JK_LOG_EMERG, "service() returned 0, error\n");  
		return JK_FALSE;
	    }
	    jk_log(l, JK_LOG_DEBUG, "Native call returned %d\n", rc);  
	    return JK_TRUE;
        }
    }
}

static int JK_METHOD done(jk_endpoint_t **e,
                          jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into done\n"); 
    if(! e || ! *e || ! (*e)->endpoint_private) {
	jk_log( l, JK_LOG_EMERG, "Done - wrong arguments \n");
	return JK_FALSE;
    }
    {
        jni_endpoint_t *p = (*e)->endpoint_private;

        if(p->attached) {
            detach_from_jvm(p->worker);
        }

        free(p);
        *e = NULL;
	jk_log(l, JK_LOG_DEBUG, "Done ok\n"); 
        return JK_TRUE;
    }
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

	jk_log(l, JK_LOG_DEBUG, "Into jni_validate\n"); 
        if(p->was_verified) {
            return JK_TRUE;
        }

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
	    jk_log(l, JK_LOG_EMERG, "Fail-> no classpath\n"); 
            return JK_FALSE;
        }

        if(jk_get_worker_jvm_path(props, p->name, &str_config)) {
            p->jvm_dll_path  = jk_pool_strdup(&p->p, str_config);
        }

        if(!p->jvm_dll_path || 
            !jk_file_exists(p->jvm_dll_path)) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> no jvm_dll_path\n"); 
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
            jk_append_libpath(&p->p, str_config);
        }


        if( ! load_jvm_dll(p, l)) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> can't load jvm dll\n"); 
	    detach_from_jvm(p);
	    return JK_FALSE;
	}

	if( ! open_jvm(p, &env, l)) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> can't open jvm\n"); 
	    detach_from_jvm(p);
	    return JK_FALSE;
	}

	if( ! get_bridge_object(p, env, l)) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> can't get bridge object\n"); 
	    detach_from_jvm(p);
	    return JK_FALSE;
	}
	
	if( ! get_method_ids(p, env, l)) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> can't get method ids\n"); 
	    detach_from_jvm(p);
	    return JK_FALSE;
	}

	p->was_verified = JK_TRUE;
	return JK_TRUE;
    }
    
}

static int JK_METHOD init(jk_worker_t *pThis,
                          jk_map_t *props, 
                          jk_logger_t *l)
{
    if(pThis && pThis->worker_private) {        
        jni_worker_t *p = pThis->worker_private;
        JNIEnv *env;

        if(p->was_initialized) {
            return JK_TRUE;
        }

        if(!p->jvm ||
           !p->jk_java_bridge_object ||
           !p->jk_service_method     ||
           !p->jk_startup_method     ||
           !p->jk_shutdown_method) {
	    jk_log(l, JK_LOG_EMERG, "Fail-> worker not set completely\n"); 
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

            if(rc) {
                p->was_initialized = JK_TRUE; 
                return JK_TRUE;
            }
	    jk_log(l, JK_LOG_EMERG, "Fail-> result from call is 0\n"); 
            return JK_FALSE;
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
        if(the_singleton_jni_worker) {
            *w = the_singleton_jni_worker;
            return JK_TRUE;
        } else {
            jni_worker_t *private_data = 
                (jni_worker_t *)malloc(sizeof(jni_worker_t ));

            if(private_data) {

                jk_open_pool(&private_data->p, 
                             private_data->buf, 
                             sizeof(jk_pool_atom_t) * TINY_POOL_SIZE);

                private_data->name = jk_pool_strdup(&private_data->p, name);          

                if(private_data->name) {
                    private_data->was_verified          = JK_FALSE;
                    private_data->was_initialized       = JK_FALSE;
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
                    the_singleton_jni_worker = &private_data->worker; 
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
    void *handle = dlopen(p->jvm_dll_path, RTLD_NOW | RTLD_GLOBAL);
    if(!handle) {
	jk_log(l, JK_LOG_EMERG, "Can't log native library %s : %s\n", p->jvm_dll_path,
	       dlerror() );  
	return JK_FALSE;
    }
    {
        jni_create_java_vm = dlsym(handle, "JNI_CreateJavaVM");
        jni_get_default_java_vm_init_args = dlsym(handle, "JNI_GetDefaultJavaVMInitArgs");
        if( jni_create_java_vm && jni_get_default_java_vm_init_args ) {
            return JK_TRUE;
        }
	jk_log(l, JK_LOG_EMERG, "Can't find JNI_CreateJavaVM or JNI_GetDefaultJavaVMInitArgs\n");
        dlclose(handle);
    }
#endif
}

static int open_jvm(jni_worker_t *p,
                    JNIEnv **env,
                    jk_logger_t *l)
{
    JDK1_1InitArgs vm_args;  
    JNIEnv *penv;
    int err;
    *env = NULL;

    vm_args.version = 0x00010001;

    if(0 != jni_get_default_java_vm_init_args(&vm_args)) {
	jk_log(l, JK_LOG_EMERG, "Fail-> can't get default vm init args\n"); 
        return JK_FALSE;
    }


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
	    jk_log(l, JK_LOG_EMERG, "Fail-> allocation error for classpath\n"); 
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

    if(err=jni_create_java_vm(&(p->jvm), 
                          &penv, 
                          &vm_args) != 0) {
	jk_log(l, JK_LOG_EMERG, "Fail-> create java vm %d \n", err); 
        return JK_FALSE;
    }

    *env = penv;

    return JK_TRUE;
}

/** Start of JDK1.2 loader ( allow -X, etc )
 */
static int open_jvm2(jni_worker_t *p,
                    JNIEnv **env,
                    jk_logger_t *l)
{
    JavaVMInitArgs vm_args;
    JNIEnv *penv;
    int err;
    JavaVMOption options[1];

    *env = NULL;

    vm_args.version = 0x00010002;
    vm_args.options = options;
    vm_args.nOptions = 1;

    /* Set classpath */
    {
	unsigned len = strlen("-Djava.class.path=") + 
	    strlen(p->tomcat_classpath) + 
	    2;
	char *tmp = jk_pool_alloc(&p->p, len);
	if(tmp) {
	    sprintf(tmp, "-Djava.class.path=%s", 
		    p->tomcat_classpath );
	    options[0].optionString = tmp;
	} else {
	    jk_log(l, JK_LOG_EMERG, "Fail-> allocation error for classpath\n"); 
	    return JK_FALSE;
	}
    }

    jk_log(l, JK_LOG_DEBUG, "Set classpath to %s\n", options[0].optionString); 

    *env = penv;

    return JK_TRUE;
}

static int get_bridge_object(jni_worker_t *p,
                             JNIEnv *env,
                             jk_logger_t *l)
{
    p->jk_java_bridge_class = (*env)->FindClass(env, JAVA_BRIDGE_CLASS_NAME);
    if(! p->jk_java_bridge_class) {
	jk_log(l, JK_LOG_EMERG, "Can't find class %s\n", JAVA_BRIDGE_CLASS_NAME); 
	return JK_FALSE;
    }
    
    {
        jmethodID  constructor_method_id = (*env)->GetMethodID(env,
                                                               p->jk_java_bridge_class, 
                                                               "<init>", /* method name */
                                                               "()V");   /* method sign */   
        if(! constructor_method_id) {
	    p->jk_java_bridge_class = NULL;
	    jk_log(l, JK_LOG_EMERG, "Can't find constructor\n"); 
	    return JK_FALSE;
	}
	{
	    p->jk_java_bridge_object = (*env)->NewObject(env, 
                                                         p->jk_java_bridge_class,
                                                         constructor_method_id);
            if(! p->jk_java_bridge_object) {
		p->jk_java_bridge_class = NULL;
		jk_log(l, JK_LOG_EMERG, "Can't create new object\n"); 
		return JK_FALSE;
	    }
	    
	    p->jk_java_bridge_object = (jobject)(*env)->NewGlobalRef(env, p->jk_java_bridge_object);
	    if(! p->jk_java_bridge_object) {
		jk_log(l, JK_LOG_EMERG, "Can't create global ref\n"); 
		p->jk_java_bridge_class = NULL;
                p->jk_java_bridge_object = NULL;
		return JK_FALSE;
	    }
	}
    }
    return JK_TRUE;
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
	jk_log(l, JK_LOG_EMERG, "Can't find startup()\n"); 
	return JK_FALSE;
    }

    p->jk_service_method = (*env)->GetMethodID(env,
                                               p->jk_java_bridge_class, 
                                               "service", 
                                               "(JJ)I");   
    if(!p->jk_service_method) {
	jk_log(l, JK_LOG_EMERG, "Can't find service()\n"); 
        return JK_FALSE;
    }

    p->jk_shutdown_method = (*env)->GetMethodID(env,
                                                p->jk_java_bridge_class, 
                                                "shutdown", 
                                                "()V");   
    if(!p->jk_shutdown_method) {
	jk_log(l, JK_LOG_EMERG, "Can't find shutdown()\n"); 
        return JK_FALSE;
    }    
    
    return JK_TRUE;
}


static JNIEnv *attach_to_jvm(jni_worker_t *p)
{
    JNIEnv *rc = NULL;

    /* It's needed only once per thread, but there is no
       generic/good way to keep per/thread data. 
    */
#ifdef LINUX
    linux_signal_hack();
#endif    

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


static void linux_signal_hack() {
    sigset_t newM;
    sigset_t old;
    
    sigemptyset(&newM);
    pthread_sigmask( SIG_SETMASK, &newM, &old );
    
    sigdelset(&old, SIGUSR1 );
    sigdelset(&old, SIGUSR2 );
    sigdelset(&old, SIGUNUSED );
    sigdelset(&old, SIGRTMIN );
    sigdelset(&old, SIGRTMIN + 1 );
    sigdelset(&old, SIGRTMIN + 2 );
    pthread_sigmask( SIG_SETMASK, &old, NULL );
}

static void print_signals( sigset_t *sset) {
    int sig;
    for (sig = 1; sig < 20; sig++) 
	{ if (sigismember(sset, sig)) {printf( " %d", sig);} }
    printf( "\n");
}

static void detach_from_jvm(jni_worker_t *p)
{
    if(p->jvm == NULL ) return;
    (*(p->jvm))->DetachCurrentThread(p->jvm);
}
