import smtplib
from email.mime.text import MIMEText
from email.header import Header
import sys


def sendEmail():
     emailSender = 'ecloud@inspur.com'
     emailContext = 'Download Url: https://fir.im/spkw'
     mailReceivers = []
     mailReceivers.append('chenmch@inspur.com')
     mailReceivers.append('zhangyj.lc@inspur.com')
     mailReceivers.append('gong_jian@inspur.com')
     mailReceivers.append('libaochao@inspur.com')
     mailReceivers.append('yufuchang@inspur.com')
     # mail instance
     emailInstance = MIMEText(emailContext, 'plain', 'utf-8')
     emailInstance['From'] = Header('ecloud@inspur.com', 'utf-8')
     emailInstance['To'] = Header('InspurInter-observer', 'utf-8')
     emailInstance['Subject'] = Header('Android Cloud + Release', 'utf-8')
     # smtp-server instance
     mail_host = 'mail.inspur.com'
     mail_port = '30'
     mail_user = 'ecloud@inspur.com'
     mail_pass = 'oND-SK3-Uxn-zD5'
     try:
        smtpOBJ = smtplib.SMTP(mail_host, mail_port)
        smtpOBJ.login(mail_user, mail_pass)
        smtpOBJ.sendmail(emailSender, mailReceivers, emailInstance.as_string())
        smtpOBJ.quit()
        print('### E-mail send succeed...')
     except smtplib.SMTPException:
        print('### E-mail send fail...')

sendEmail();