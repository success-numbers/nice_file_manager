FROM fauria/vsftpd

# Add a user with a password (replace 'ftpuser' and 'password' with your desired username and password)
RUN useradd -m -d /home/ftpuser -s /bin/bash ftpuser && \
    echo "ftpuser:password" | chpasswd

# Expose ports 20 and 21
EXPOSE 20 21

# Expose passive ports
EXPOSE 21100-21110

# Create home directory and set permissions
RUN mkdir -p /home/ftpuser/ftp && chown -R ftpuser:ftpuser /home/ftpuser/ftp

# Copy vsftpd configuration file
COPY vsftpd.conf /etc/vsftpd/vsftpd.conf

# Set the default command to run vsftpd
CMD ["/usr/sbin/vsftpd", "/etc/vsftpd/vsftpd.conf"]
