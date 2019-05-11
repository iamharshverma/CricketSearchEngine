import os
import smtplib

sent_subject = "Alert on CrawlJob : VAtsal SYS"
sent_body = "\nAlerts as  follows : VAtsal SYS \n\n"
#ps aux | grep crawl 
#if length is greated than zero means a job is currently running, exit
crawlJobs = os.popen("ps aux | grep crawl ").read()
if len(crawlJobs) > 0:
	sent_body = ('\n\nExisting since previous crawl job is still in process\n')
	sent_subject = "Alter on CrawlJob : Skipping this Job"

#if count of parsed is less than count of failed Send out email with URGENT SIBJECT and error output
#cat crawlScriptOutput.txt | grep Parsed | wc -l
#cat crawlScriptOutput.txt | grep failed | wc -l
parsedCount = os.popen("cat crawlScriptOutput.txt | grep Parsed | wc -l").read()
failedCount = os.popen("cat crawlScriptOutput.txt | grep failed | wc -l").read()
print(parsedCount)
print(failedCount)
if (int(parsedCount) < int(failedCount)):
	sent_body = sent_body + "\n" + ("parsed count greater than failed count")


#count number of new lines in parsed and return parseds
#cat crawlScriptOutput.txt | grep \(add\/update\)
indexed = os.popen("cat crawlScriptOutput.txt | grep \\(add\\/update\\)").read()
sent_body = sent_body + "\n" + (indexed)
#add this to message

indexed = os.popen("cat crawlScriptOutput.txt | grep Iteration").read()
sent_body = sent_body + "\n" + (indexed)

indexed = os.popen("cat crawlScriptOutput.txt | grep \"Finished loop\"").read()
sent_body = sent_body + "\n" + (indexed)

exitError = os.popen("cat crawlScriptOutput.txt | grep \"Failed with exit value\"").read()
if len(exitError) > 0:
	sent_body = sent_body + "\n" + ("Existed with Error Check Quickly") + "\n" + exitError
	sent_subject = "Alter on CrawlJob : URGENT Error ocurred"

#if output of follwing command is greater than zero send out email with URGENT SUBJECT and error output
#cat crawlScriptOutput.txt | grep "Error running"
errorUrgent = os.popen("cat crawlScriptOutput.txt | grep \"Error running\"").read()
if len(errorUrgent) > 0:
	sent_body = sent_body + "\n" + (errorUrgent)  + "\n" + errorUrgent
	sent_subject = "Alter on CrawlJob : URGENT Error ocurred"

errorUrgent = os.popen("cat crawlScriptOutput.txt | grep \" Unable to bind to locking port 7054\"").read()
if len(errorUrgent) > 0:
        sent_body = sent_body + "\n" + (errorUrgent)  + "\n" + errorUrgent
        sent_subject = "Alter on CrawlJob : URGENT Error ocurred"

#count of urls failed
# cat crawlScriptOutput.txt | grep failed
failed = os.popen("cat crawlScriptOutput.txt | grep failed").read()
sent_body = sent_body + "\n" + (failed)

#usename , password
gmail_user = 'harshverma59@gmail.com'
gmail_app_password = 'uzmafkucnemxzslq'

# =============================================================================
# SET THE INFO ABOUT THE SAID EMAIL
# =============================================================================
sent_from = gmail_user
sent_to = ['harshverma59@gmail.com']
# sent_subject = "Testing Email Script"

email_text = """\
From: %s
To: %s
Subject: %s

%s
""" % (sent_from, ", ".join(sent_to), sent_subject, sent_body)

# =============================================================================
# SEND EMAIL OR DIE TRYING!!!
# Details: http://www.samlogic.net/articles/smtp-commands-reference.htm
# =============================================================================

try:
    server = smtplib.SMTP_SSL('smtp.gmail.com', 465)
    server.ehlo()
    server.login(gmail_user, gmail_app_password)
    server.sendmail(sent_from, sent_to, email_text)
    server.close()

    print('Email sent!')
except Exception as exception:
    print("Error: %s!\n\n" % exception)
