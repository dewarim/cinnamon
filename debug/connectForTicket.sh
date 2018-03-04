#!/usr/bin/env bash

# debugging script: connect to server.


set -e

echo "Admin Ticket:"
    curl -d user=admin -d pwd=admin http://localhost:8080/cinnamon/cinnamon/connect
    
    # expected: something like
    # <connection><ticket>698cbb7d-f930-4177-980a-014bd6206d36@localhost</ticket></connection>
    
echo ""    
echo "Unknown user"
    curl -d user=unknown-user -d pwd=admin http://localhost:8080/cinnamon/cinnamon/connect
    
    # expected:
    # <error><code>error.user.not.found.and.ldap.create.failed</code><message>error.user.not.found.and.ldap.create.failed</message></error>

echo ""
echo "LDAP user"
    curl -d "user=John Doe" -d "pwd=Dohn.Joe_1" http://localhost:8080/cinnamon/cinnamon/connect
    
    # expected:
    # if previously unknown, a new user should be created.
    # otherwise, return ticket for user.

echo ""