#include <jni.h>
#include "stdio.h"
#include "stdlib.h"
#include "fcntl.h"
#include "door.h"

#undef org_apache_tomcat_service_Door_DOOR_PRIVATE
#define org_apache_tomcat_service_Door_DOOR_PRIVATE 2L
#undef org_apache_tomcat_service_Door_DOOR_UNREF
#define org_apache_tomcat_service_Door_DOOR_UNREF 1L

/*
 * Class:     org_apache_tomcat_service_Door
 * Method:    open
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_tomcat_service_Door_open(JNIEnv *env, jclass thisC, jstring jname) {
    jbyte *name;
    jlong fd;
    
    name=(jbyte *)(*env)->GetStringUTFChars( env, jname, NULL );
    
    printf("Opening \n");
    fd=(jlong)open( (const char *)name, O_RDONLY );

    (*env)->ReleaseStringUTFChars( env, jname, (const char *)name);
    return fd;
}

/*
 * Class:     org_apache_tomcat_service_Door
 * Method:    close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_apache_tomcat_service_Door_close(JNIEnv *env, jclass thisC, jlong jid) {

    printf("Closing\n");
    close( (int)jid ); 
}

/*
 * Class:     org_apache_tomcat_service_Door
 * Method:    call
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tomcat_service_Door_call(JNIEnv *env, jclass thisC, jlong jid, jbyteArray jarg, jint jsize) {
    jbyte *barr;
    door_arg_t args;
    int size=(int)jsize;

    barr=(*env)->GetByteArrayElements( env, jarg, NULL );
    size=2;
    
    args.data_ptr = (char*)barr;
    args.data_size = (int)size;
    args.desc_ptr = NULL;
    args.desc_num = 0;
    args.rbuf = (char *)barr;
    args.rsize = (int)size; /* maybe arg length? */

    printf("Calling %d %lx\n", jid, &args);
    printf("Before %lx %lx %d %d %d\n", args.data_ptr, args.rbuf, args.data_size, args.rsize, (int)barr[0]);
    door_call( (int)jid, &args );
    printf("Ok %lx %lx %d %d %d\n", args.data_ptr, args.rbuf, args.data_size, args.rsize, (int)barr[0]);
    
    (*env)->ReleaseByteArrayElements( env, jarg, barr, 0); 
    return args.data_size;
}

JNIEXPORT jint JNICALL Java_org_apache_tomcat_service_Door_info(JNIEnv *env, jclass thisC, jlong jid) {
    struct door_info info;

    printf("Calling door info\n");
    door_info( (int)jid, &info );
    printf("Ok PID=%ld\n", (long)info.di_target );
    return (jint)info.di_target;
/*     (*env)->ReleaseByteArrayElements( env, jarg, barr1, 0); */
}

JNIEnv *saved_JNIEnv;
JavaVM *saved_JVM;
char *handlerC="doors/Door";
char *mname="handler";

void doorProc( void * cookie, char *data, size_t datasize, 
	       door_desc_t *desc, size_t ndesc) {
    jint jerr;
    char arg;
    JNIEnv *env;
    jint res;
    jclass cls;
    jmethodID mid;

    printf("In door %d\n", (int) datasize);

    arg= *((char *)data);
    printf("Arg %d\n", (int) arg);

    jerr = (*saved_JVM)->AttachCurrentThread( saved_JVM, &env, NULL);
    printf("Attached to JVM thread %d %lx\n", jerr, env);
    
    printf("Calling %s %s\n", handlerC, mname);
    
    /*   printf("handler: %s\n", hclass);  */
    cls = (*env)->FindClass(env, handlerC);
    
    if (cls == 0) {
	printf( "Can't find handler class\n");
	return ;
    }
    
    mid = (*env)->GetStaticMethodID(env, cls, mname, "(J)I");
    
    if (mid == 0) {
	printf( "Can't find handler1 method\n");
	return ;
    }
    
    res= (*env)->CallStaticIntMethod(env, cls, mid, (jlong)1 );
    printf("Done handler: %d\n", res);  
    
    data[0] = res;
    jerr = (*saved_JVM)->DetachCurrentThread( saved_JVM);
    
    door_return( data, datasize, NULL, 0);
    printf("Door return\n");
}


/* XXX. Add cookies, support multiple door entries, etc */
JNIEXPORT jlong JNICALL Java_org_apache_tomcat_service_Door_create(JNIEnv *env, jclass thisC, jstring jname, jobject jfunc, jint jflags) {
    int fd, fd1;
    jbyte *name;
    JavaVM *jvms[2];
    jsize  nrOfVM;
    jint jerr;

    jerr = JNI_GetCreatedJavaVMs(jvms, 1, &nrOfVM);
    if( nrOfVM>0 ) {
     	saved_JVM=jvms[0];
    } else {
	/* XXX todo: throw  */
	return (jlong)-1;
    }
    
    fd=door_create( doorProc, NULL, 0); 

    saved_JNIEnv=env;
    name=(jbyte *)(*env)->GetStringUTFChars( env, jname, NULL );
    
    unlink(name); 
    fd1=creat((const char *)name,  0600 ); 
    close(fd1); 
    
    printf("Created file\n");
    fattach( fd, name);
    printf("Attached\n");

    (*env)->ReleaseStringUTFChars( env, jname, (const char *)name);
    
    
    return (jlong)fd;
}

JNIEXPORT jlong JNICALL Java_org_apache_tomcat_service_Door_destroy(JNIEnv *env, jclass thisC, jlong fd, jstring jname) {
    jbyte *name;

    close( (int)fd );
    
    name=(jbyte *)(*env)->GetStringUTFChars( env, jname, NULL );
    
    printf("Detaching door %s\n", name);
    fdetach(name);
    printf("Unlinking %s\n", name);
    unlink(name);

    (*env)->ReleaseStringUTFChars( env, jname, (const char *)name);
    
    
    return (jlong)0;
}
