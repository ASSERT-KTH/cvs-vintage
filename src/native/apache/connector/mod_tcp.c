/*
 * XXX copyright
 */

#include "httpd.h"
#include "http_config.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
#include "util_md5.h"

/** 
    XXX Add high level description
    XXX Add configuration documentation
*/

module tcp_connector_module;

#define MAX_HOSTS 20
/* 16 k */
#define MAX_BUFF_SIZE 0x4000
#include "msg_buffer_simple.c"
#include "rmethods.c"
#include "connection_tcp.c"
#define DEFAULT_ARG1 "localhost"
#define DEFAULT_ARG2 "8007"

#define CONNECTOR "Tcp"
#define CONNECTOR_MODULE tcp_connector_module
#define CONNECTOR_HANDLE_NAME "tcp_handler"
#include "module_c.c"



