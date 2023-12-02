public interface TCPConnectionListener {
    void onConnectionRead(TCPConnection tcpConnection);
    void onReceiveMessage(TCPConnection tcpConnection, Message value);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception e);
}
