import socket
import threading
import random

# connection setting
HOST = '127.0.0.1'
PORT = 22000
UDP_PORT = random.randint(40000, 50000)
server_address = (HOST, PORT)

# open tcp connection
tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcp_sock.connect(server_address)

# open udp connection
udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
udp_sock.bind((HOST, UDP_PORT))
# synchronize message
udp_sock.sendto(bytes(str('[HELLO]'), encoding='utf8'), (HOST, PORT))

print("Please give me your name:")
name = input()
send_mode = "TCP"

print("Send mode TCP")


def handle_messages_tcp(conn):
    while True:
        try:
            msg = conn.recv(1024)
            msg = str(msg, "utf8")
            print(msg)
            if not msg:
                break

        except socket.error as ex:
            print("Received error occured")
            print(ex)
            break

    print("exit tcp...")


def handle_messages_udp(conn):
    while True:
        try:
            buff, address = conn.recvfrom(1024)
            msg = str(buff, 'utf8')
            print(msg)
            if not msg:
                break

        except socket.error as ex:
            print("Received error occured")
            print(ex)
            break

    print("exit udp...")


threading.Thread(
    target=handle_messages_tcp,
    kwargs={'conn': tcp_sock}
).start()

threading.Thread(
    target=handle_messages_udp,
    kwargs={'conn': udp_sock}
).start()

try:

    while True:
        # Send data
        msg = input()

        if msg == "U":
            send_mode = "UDP"
            print("Send mode set to UDP")
        elif msg == "T":
            send_mode = "TCP"
            print("Send mode set to TCP")
        elif msg == "M":
            send_mode = "MCT"
            print("Send mode set to Multicast")
        else:
            if send_mode == "UDP":
                udp_sock.sendto(bytes(str(name) + ": " + msg, encoding='utf8'), (HOST, PORT))
            elif send_mode == "TCP":
                message = bytes(str(name) + ": " + msg, encoding='utf8')
                tcp_sock.send(message)
            elif send_mode == "MCT":
                pass


finally:
    tcp_sock.close()
    udp_sock.close()
