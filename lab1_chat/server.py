import socket
import threading

# connection setting
HOST = '127.0.0.1'
PORT = 22000
current_user_id = 0
users_tcp = []
users_udp = []


def client_thread_service(connection, user_id):
    while True:
        try:
            data = connection.recv(1024)
            if not data:
                break

            print("Message from " + str(user_id) + " " + str(data, "utf8"))
            for con in users_tcp:
                if con is not None:
                    if con[1] != user_id:
                        con[0].send(data)

        except Exception as ex:
            print("Client Thread error ", user_id, ": ", ex)
            connection.close()
            users_tcp.remove((connection, user_id))
            break

    connection.close()
    print(users_tcp)


def handle_udp_clients(sock):
    while True:
        buff, address = sock.recvfrom(1024)
        print("udp: " + str(buff, 'utf8'))
        if address not in users_udp:
            users_udp.append(address)

        # '[HELLO]' is message to synchronize connection
        if str(buff,'utf8') != '[HELLO]':
            for add in users_udp:
                if add != address:
                    sock.sendto(buff, add)


def get_id_for_next_client():
    global current_user_id
    current_user_id = current_user_id + 1
    return current_user_id


# MAIN PROGRAM

if __name__ == "__main__":

    tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    try:
        udp_socket.bind((HOST, PORT))
        tcp_socket.bind((HOST, PORT))
        tcp_socket.listen()

    except socket.error as e:
        print("Error", (str(e)))
        quit()

    print("Server start...")
    print("Listen on " + str(HOST) + ":" + str(PORT) + " ...")

    threading.Thread(
        target=handle_udp_clients,
        kwargs={'sock': udp_socket}
    ).start()

    while True:

        try:
            conn, add = tcp_socket.accept()
            print("New connection with:", add)
            id_for_next_client = get_id_for_next_client()
            users_tcp.append((conn, id_for_next_client))
            threading.Thread(
                target=client_thread_service,
                kwargs={'connection': conn, 'user_id': id_for_next_client}
            ).start()

        except socket.error as e:
            print("Error", (str(e)))
            quit()
