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
 * Description: Replaces @VARIABLE@ values made by AutoMake/Conf for Win32   *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.2 $                                             *
 *****************************************************************************/
#include <stdio.h>
#include <string.h>

int main(int argc, char *argv[]) {
    char *line=(char *)malloc(1024*sizeof(char));
    char *name[128];
    char *data[128];
    char *temp;
    int defs;
    int x,y,k;

    /* Check for proper command line arguments */
    if (argc<2) {
        printf("Usage: %s [name=data] ... < [inputfile] > [outputfile]\n",
               argv[0]);
        return(1);
    }

    /* Parse command line arguments to translate NAME=VALUE in @NAME@, VALUE */
    for (k=1;k<argc;k++) {
        char *n=name[k-1];
        char *d=name[k-1];
        char *a=argv[k];
        x=0; y=0;


        n=(char *)malloc(128*sizeof(char));
        d=(char *)malloc(128*sizeof(char));
        n[0]='@';
        while ((a[x]!='\0')&&(a[x]!='=')) {
            n[x+1]=a[x++];
        }
        n[x+1]='@'; n[x+2]='\0';
    
        if(a[x]=='=') x++;
        while(a[x]!='\0') {
            d[y++]=a[x++];
        }
        d[y]='\0';
        name[k-1]=n;
        data[k-1]=d;
    }

    /* Print all replacing rules to standard error */
    defs=k-1;
    /* fprintf(stderr,"%d replacing rules defined\n",defs);
     * for (k=0;k<defs;k++) {
     *   fprintf(stderr,"Replacing \"%s\" with \"%s\"\n",name[k],data[k]);
     * }
     */

    /* Start examining lines */
    x=0;
    while(fgets(line, 1024, stdin)!=NULL) {
        /*   Increase line count */
        x++;
        /* Copy the line in a temporary buffer */
        temp=(char *)malloc((strlen(line)+1)*sizeof(char));
        strcpy(temp,line);
        /* In each line check for every name. */
        for (k=0;k<defs;k++) {
            char *ret=NULL;
            if ((ret=strstr(temp,name[k]))!=NULL) {
		/* We got a match of a name
                 * Evaluate string lengths */
                int namelen=strlen(name[k]);
                int datalen=strlen(data[k]);
                int linelen=strlen(temp);
                int len=linelen-namelen+datalen;
                int beglen=(int)(ret-temp);
                int endlen=linelen-beglen-namelen;
                /* Allocate some memory for the string */
                char *buf=(char *)malloc((len+1)*sizeof(char));
                /** fprintf(stderr,"[Line %d] Replaced \"%s\" with \"%s\"\n",
                 *        x,name[k],data[k]);
                 * Copy the beginning part, change the name and copy the rest
		 */
                strncpy(buf,temp,beglen);
                strncpy(buf+beglen,data[k],datalen);
                strncpy(buf+beglen+datalen,temp+beglen+namelen,endlen);
                /* Terminate the string */
                buf[len]='\0';
                /* Deallocate unused memory and replace the string */
                free(temp);
                temp=buf;
            }
                    
        }
        /* Print out the line */
        fprintf(stdout,"%s",temp);
        /* Deallocate temporary buffer */
        free(temp);
    }
    return(0);
}

