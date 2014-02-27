FROM ubuntu

MAINTAINER Tobias Lindaaker tobias@thobe.org

# create a user
RUN adduser --system --disabled-password ircd

# copy the irc server
ADD ircd.py /home/ircd/
RUN chown ircd /home/ircd/ircd.py
RUN chmod +x /home/ircd/ircd.py

# expose the IRC port
EXPOSE 6667

# run the server
USER ircd
CMD ["/home/ircd/ircd.py"]
