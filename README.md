# Secure-Chat

## How it works
Created a Chat that the people connect to it can send and read messages from other people.

The Chat is encrypted with AES encryption.

Each message is encrypted and sent to the server. When the server receive the message, it decrypted the message and send to all the clients connected on chat.

All the clients that receive the encrypted message from the server, decrypts it and show the real message.

When client clicks on "Sair" a message is sent to the server to warn every connected client about that client exit.


