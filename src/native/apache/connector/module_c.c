/* Apache-specific code, to be included in modules 
   A better aproach to OO in C is needed ( instead of include and define )...
*/

/*
  module doors_connector_module;
*/


typedef struct {
    /**  Methods */

    /* "public" fields, in all protocol modules */
    int debug;
    pool *config_pool;
    server_rec *server_rec;

    int hostc;

    RemoteHost **hosts; 
} Connector;


/* ---------------------------------------- */
static int connector_fixups(request_rec *r) {
    if( (r->content_type != NULL ) ||
	(r->handler != NULL ) ) 
	return DECLINED;

    /* File not found - it may be a servlet */
    /* XXX Doing that means File not found will be handled by tomcat */
    printf("FIXUP: %s %s \n", r->filename , r->unparsed_uri );

    r->handler = CONNECTOR_HANDLE_NAME ; 
    
    return DECLINED;
}

static int connector_handler(request_rec *r) {

    void *sess;
    MsgBuffer *msg;
    Connector *rpm;
    RemoteHost *rhost;
    Connection *cn;
    int err;

    rpm=(Connector *)ap_get_module_config(r->server->module_config,
						     & CONNECTOR_MODULE );
    /* A load-balancing module will set a note with the rhost to use,
       we'll search it in hosts */
    rhost=rpm->hosts[0];

    /* XXX Send only the relevant information  */
    ap_add_cgi_vars(r);
    ap_add_common_vars(r);
    
    msg = b_new( r->pool );
    b_set_buffer_size( msg, MAX_BUFF_SIZE); 

    b_reset( msg );
    encode_request( msg , r );
    
    cn=connection_get( rhost );
    if(cn==NULL) {
	return NOT_FOUND;
    }
    err= connection_send_message( cn, msg );
    
    if(err<0) {
	/* Disconect */
	connection_destroy( cn );
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error sending request %d\n", err);
	/* XXX retry once !!! */
	
	return NOT_FOUND;
    }

    while( 1 ) {
	int err=connection_get_message( cn, msg );
	/* 	b_dump(msg, "Get Message: " ); */
	if( err < 0 ) {
	    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error reading request %d\n", err);
	    // XXX cleanup, close connection if packet error
	    connection_destroy( cn );
	    return NOT_FOUND;
	}
	if( b_getCode( msg ) == END_RESPONSE )
	    break;
	err=process_callback( msg, r );
	if( err == HAS_RESPONSE ) {
	    err=connection_send_message( cn, msg );
	    if( err < 0 ) {
		ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			      "Error reading response1 %d\n", err);
		connection_destroy( cn );
		return NOT_FOUND;
	    }
	}
	if( err < 0 ) break; /* XXX error */
    }

    connection_release( cn );

    return OK;
}

/* ---------------------------------------- */

/**
   Standard Apache configuration handling - create and maintain config struct
   Configuration directives
*/
static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *arg1, char *arg2);

static void *create_connector_config(pool *p, server_rec *server)
{
    Connector *rpm = (Connector *) ap_palloc(p, sizeof(Connector));
    
    rpm->config_pool=p;
    rpm->debug=1;
    rpm->server_rec=server;
    rpm->hosts = (RemoteHost **)ap_palloc( rpm->config_pool, MAX_HOSTS * sizeof( void *) );

    add_host( NULL, rpm, "default", DEFAULT_ARG1, DEFAULT_ARG2 );

    /* XXX check alloc error, return NULL if any */
    return rpm;
}

/* XXX Not fully implemented */
static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *arg1, char *arg2)
{
    /* XXX !!! Error checking - return real message since it's a config problem*/    
    Connector *rpm=(Connector *)module_c;
    
    rpm->hosts[0]= rhost_new( rpm->config_pool, name , arg1, arg2);

    if( rpm->debug ) ap_log_error( APLOG_MARK, APLOG_EMERG, rpm->server_rec,
				   "Add host %s %s", name, arg1);
	
    return NULL;
}

static const char *set_debug(cmd_parms *cmd, void *module_c, char *arg1)
{
    Connector *rpm=(Connector *)module_c;
    rpm->debug=atoi(arg1);
    return NULL;
}

/* ---------------------------------------- */
/**
   Module registration data ( sort of Module Interface ) 
*/

/* List of handlers */

static handler_rec connector_handlers[] = {
    {CONNECTOR_HANDLE_NAME , connector_handler}, // XXX remove, separate module
    {NULL}
};

/* List of configuration directives  */

static command_rec connector_cmds[] = {
    {CONNECTOR "Connector", add_host , NULL, RSRC_CONF, TAKE23,
     "Set a connector, default is apconnector "},
    {CONNECTOR "ConnectorDebug", set_debug, NULL, RSRC_CONF, TAKE1,
     "Debug level for Connector"},
    {NULL}
};

/* Apache module descriptor */

/* Assume at least Apache 1.3 - no check for >19970622 ( it was removed from 
  most Apache modules I saw) */
module MODULE_VAR_EXPORT CONNECTOR_MODULE = {
    STANDARD_MODULE_STUFF,
    NULL,                       /* module initializer */
    NULL,                       /* per-directory config creator */
    NULL,                       /* dir config merger */
    create_connector_config,       /* server config creator */
    NULL,                       /* server config merger */
    connector_cmds,                /* command table */
    connector_handlers,            /* [7] list of handlers */
    NULL,                       /* [2] filename-to-URI translation */
    NULL,                       /* [5] check/validate user_id */
    NULL,                       /* [6] check user_id is valid *here* */
    NULL,                       /* [4] check access by host address */
    NULL,                       /* [7] MIME type checker/setter */
    connector_fixups,                       /* [8] fixups */
    NULL,                       /* [10] logger */
    NULL,                       /* [3] header parser */
    NULL,                       /* apache child process initializer */
    NULL,                       /* apache child process exit/cleanup */
    NULL                        /* [1] post read_request handling */
};


