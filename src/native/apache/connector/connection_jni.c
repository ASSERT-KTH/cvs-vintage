/* Incomplete, doesn't compile - initial code to 
   support embedded JVM and "direct" communication */

#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "util_script.h"

#include <stdio.h>
#include <jni.h>

/* pointer 2 long, long 2 pointer, long 2 req */
#define P2L(x) (jlong)(unsigned long)(x)
#define L2P(x) (void *)(unsigned long)(x)
#define L2R(x) (request_rec *)(unsigned long)(x)

#define OPTIONS_INIT_SIZE 20

#define SERVER_CONF 1
#define DIR_CONF 2

typedef struct javacfg {
  int type;
  // global options
  array_header *options; // global options
  array_header *jvmOptions; // options for jvm
  char *classpath;
  int verbose;

  char *handler; // used in per/directory configs
} javacfg;

JNIEnv* embjava_createJVM(javacfg *cfg);

static JavaVM *embjava_jvm;


/** Get started JVM or create a new JVM */
#ifdef GET_CREATED_JVM
JNIEnv* embjava_getJavaVM() {
    JavaVM *jvms[2];
    jsize  nrOfVM;
    jint jerr;

    jerr = JNI_GetCreatedJavaVMs(jvms, 1, &nrOfVM);
    if( nrOfVM>0 ) {
      return jvms[0];
    } else {
      // todo: log
      return NULL;
    }
}
#else
JNIEnv* embjava_getJavaVM() {
  return embjava_jvm;
}
#endif


/** Create a JVM
 *  Sets embjava_jvm global variable.
 */ 
JNIEnv* embjava_createJVM(javacfg *cfg) {
  JNIEnv *env;
  /*   JavaVM *jvm; */
  JDK1_1InitArgs vm_args;
  jint res;

  table_entry *entries=(table_entry *)cfg->options->elts;

  vm_args.version = 0x00010001;
  
  JNI_GetDefaultJavaVMInitArgs(&vm_args);
  vm_args.classpath = cfg->classpath;
  if(cfg->verbose) {
      vm_args.verbose = 1;
      vm_args.vfprintf = embjava_jlog; 
  }

  printf("JVM: %s", vm_args.classpath);
  res = JNI_CreateJavaVM(&embjava_jvm,&env,&vm_args);
  if (res < 0) {
    embjava_log( "Can't create Java VM!\n");
    return NULL;
  }
  embjava_trace("After create JVM\n"); 
  return env;
}
#endif


jint embjava_callHandler(jint type, jlong p1, char *handlerC, char *mname) {
  // todo: cache the pointers - it might break due to bugs in some VMs 
  // ( I had the problem with VAJ, I hope it's fixed now )
  JNIEnv *env;
  JavaVM *jvm;
  jint jerr; 
  jint res;
  jclass cls;
  jmethodID mid;

  printf("Calling %s %s\n", handlerC, mname);
  // ok, we should have a VM by now

  jvm=embjava_getJavaVM();
  if( jvm== NULL) return DECLINED;
  
  jerr = (*jvm)->AttachCurrentThread( jvm, &env, NULL);
  //  if( jerr != 0 ) return DECLINED;

  if( env==NULL) {
    return DECLINED;
  }
  
  /*   printf("handler: %s\n", hclass);  */
  cls = (*env)->FindClass(env, handlerC);

  if (cls == 0) {
    embjava_log( "Can't find handler class");
    return DECLINED;
  }
  mid = (*env)->GetStaticMethodID(env, cls, mname, "(IJ)I");
  if (mid == 0) {
    embjava_log( "Can't find handler1 method");
    return DECLINED;
  }
  res= (*env)->CallStaticIntMethod(env, cls, mid, type, p1);
  /*   printf("Done handler: %d\n", res);  */
  jerr = (*jvm)->DetachCurrentThread( jvm);
  return res;
}


static jint embjava_callHandler0(server_rec *s) {
  javacfg *cfg = (javacfg *)ap_get_module_config(s->module_config, &embjava_module);

  return embjava_callHandler( 0, P2L(cfg->options), hclass, "handler1");
}


static jint embjava_callHandler1(jint type, request_rec *r) {
  javacfg *cfg = (javacfg *)ap_get_module_config(r->per_dir_config, &embjava_module);

  if(cfg==NULL)
    printf("Config is null\n");
  if( (cfg!=NULL) && (cfg->handler!=NULL)) {
    printf("CH1: %s\n", cfg->handler);
    return embjava_callHandler( type, P2L(r), cfg->handler, "handler1");
  }
  // else use global (default) handler
  printf("CH2: %s\n", hclass);
  return embjava_callHandler( type, P2L(r), hclass, "handler1");
}
