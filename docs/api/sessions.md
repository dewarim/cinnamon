# Sessions

This is a high-level overview of an element of the Cinnamon server API. 
Please refer to the JavaDocs for more details like the exact parameter and options list.

The session methods live in the server.CmdInterpreter class (in Cinnamon 2).

## connect

The connect method expects the username and password and will return a session ticket (an
UUID which is a valid access token for the chosen repository).
Much like a session cookie, the ticket has to be used in every subsequent method call, until 
the user ends the session.

## forkSession

Returns a second session ticket for the owner of the current ticket. This is useful for
multi-threaded clients which spawn additional threads that will disconnect from the server
after their unit of work is completed.

## disconnect

Terminate a current session. Always returns a success message, even if the ticket is no longer valid.

