# python script
#
# move all files to new folder "mail"
#


import java.lang as javalang
import os
import sys
import re
import string

def move_file(src,dest):
  src_ptr = open(src, 'rb')
  dest_ptr = open(dest,'wb')
  dest_ptr.write( src_ptr.read() )  
  src_ptr.close()
  dest_ptr.close()  
  os.remove(src)

def create_mail_dir():
  os.mkdir(sys.argv[1]+"/mail")
  

def move_pop3(directory_name):
  # full path of old mailbox
  src_dir = sys.argv[1]+"/"+directory_name

  # full path of newmailbox 
  dest_dir = sys.argv[1]+"/mail/"+directory_name

  print "moving pop3:"+directory_name+" to destination:"+dest_dir  

  # create directory
  os.mkdir(dest_dir)

  # get file listing
  message_list = os.listdir(src_dir)

  # move each file to new location
  for i in message_list:
    move_file(src_dir+"/"+i,dest_dir+"/"+i)
   
  # remove old pop3server-directory
  os.rmdir(src_dir)
 
def move_mailbox(directory_name):
  # full path of old mailbox
  src_dir = sys.argv[1]+"/"+directory_name

  # full path of newmailbox 
  dest_dir = sys.argv[1]+"/mail/"+directory_name

  print "moving mailbox:"+directory_name+" to destination:"+dest_dir  

  # create directory
  os.mkdir(dest_dir)

  # get file listing
  message_list = os.listdir(src_dir)

  # copy each file to new location
  for i in message_list:
    if i=="header": 
      # rename "header" to ".header"
      move_file(src_dir+"/"+i,dest_dir+"/."+i)

    elif re.search("^message",i):
      # rename "message132" to "132"
      pattern_compile=re.compile("message")
      new_filename = pattern_compile.sub("",i,0)
      move_file(src_dir+"/"+i,dest_dir+"/"+new_filename)
    
  # remove old mailbox-directory
  os.rmdir(src_dir)

#print sys.argv[0]
user_dir = __columba__
print user_dir
sys.argv.append(user_dir)

if len(sys.argv) < 2:
  print "usage: convert.py <directory-name>"
  sys.exit(1)
  
print "Starting import-script..."
print "Your config-files need to be converted."

# create directory "mail"
create_mail_dir()

# get directory list of config-folder
print "directory list:"+sys.argv[1]
list = os.listdir(sys.argv[1])

# find every file/directory in config-directory
# which needs to be moved to mail
for i in list:
  match = re.search("\d",i)
  if match:
    if i=="log4j.properties":
      print "no match"
    elif i=="pop3server":
      move_pop3(i)
    else:
      move_mailbox(i)
  else:
    xml_match = re.search("xml\Z",i)
    if xml_match:
      if i=="options.xml":
        print "other: "+i+" ..ignoring..."
      else:
        move_file(sys.argv[1]+"/"+i,sys.argv[1]+"/mail/"+i)
    else:
      print "other: "+i+" ..ignoring..."
    
