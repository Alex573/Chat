# Chat
simpleserver
/ **
 * Redesigned user commands:
 * new commands work with the database (new user, delete, print, change password)
 * new commands for sending messages to all or one
 * input in the server is checked and if this is not the command the message is displayed or if the wrong command is also output
 * the process of closing the server has also been completed (clients are disconnected, the processes are disabled)
 * messages sent by clients are sent to all clients
 * added how many clients are in the chat when you enter new and exit
 * the authorization is completed to enter without a login and the correct password can not be ---- "probably".
 * Added the ability to send the client to the client personally and the client to the server personally and the server to the client.
 * added encryption.
 * /
class MakeDBFile
/ **
 * Added 4 methods
 * adding a new user
 * Deleting a user
 * Change Password
 * print the database
 * in the first 3 methods, after each database change, a new database is printed
 *
 *
 * /
in the database login password
lex 123
lex1 456
lex2 789
