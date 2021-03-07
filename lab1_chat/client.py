import socket
import threading
import random
import struct

# connection settings
HOST = '127.0.0.1'
PORT = 22000
UDP_PORT = random.randint(40000, 50000)
MCAST_GRP = '224.1.1.1'
MCAST_PORT = 5007
MULTICAST_TTL = 2
server_address = (HOST, PORT)

# open tcp connection
tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcp_sock.connect(server_address)

# open udp connection
udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
udp_sock.bind((HOST, UDP_PORT))
# synchronize message
udp_sock.sendto(bytes(str('[HELLO]'), encoding='utf8'), (HOST, PORT))

# multicast connection

# sending socket
mct_sock_send = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
mct_sock_send.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, MULTICAST_TTL)

# receiving socket
mct_sock_recv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
mct_sock_recv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
mct_sock_recv.bind(('', MCAST_PORT))
mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
mct_sock_recv.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)


print("Please give me your name:")
name = input()
send_mode = "TCP"

print("Send mode TCP")

def handle_messages(conn, type):
    while True:
        try:
            msg = conn.recv(1024)
            msg = str(msg, "utf8")
            if not name + ':' in msg:
                print(msg)
            if not msg:
                break

        except socket.error as ex:
            print("Received error occured")
            print(ex)
            break

    print("exit " + type + '...')


threading.Thread(
    target=handle_messages,
    kwargs={'conn': tcp_sock, 'type': 'tcp'}
).start()

threading.Thread(
    target=handle_messages,
    kwargs={'conn': udp_sock, 'type': 'udp'}
).start()

threading.Thread(
    target=handle_messages,
    kwargs={'conn': mct_sock_recv, 'type': 'multicast'}
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
                mct_sock_send.sendto(bytes(str(name) + ": " + msg, encoding='utf8'), (MCAST_GRP, MCAST_PORT))


finally:
    tcp_sock.close()
    udp_sock.close()
    mct_sock_recv.close()
    mct_sock_send.close()

# sources
# https://stackoverflow.com/questions/603852/how-do-you-udp-multicast-in-python/1794373
# https://wiki.python.org/moin/UdpCommunication