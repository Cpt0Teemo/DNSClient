import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Iterator;
import java.net.*;

class DnsClient{

  public static void main(String[] args) {
    int sizeOfOptions = args.length;

    // Command line input requires minimum 2 variables maximum 10 variables
    if(sizeOfOptions < 2 || sizeOfOptions > 10){
      System.out.println("ERROR: Incorrect number of inputs...");
      System.exit(0);
    }
    
    Iterator<String> arguments = Arrays.stream(args).iterator();
        
    DnsRequest request = new DnsRequest(arguments);

    sendUdpRequest(request);
  }

  private static void sendUdpRequest(DnsRequest request)
  {
    DatagramSocket clientSocket = null;
    try {
      InetAddress IPAddress = InetAddress.getByAddress(request.getServerIpBytes());
      clientSocket = new DatagramSocket();

      byte[] sendData = request.createDnsRequest();
      byte[] receiveData = new byte[1024];

      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, request.getPort());

      clientSocket.send(sendPacket);

      System.out.println("Sending: " + sendData.length + " bytes");
      for (int i = 0; i< sendData.length; i++) {
        System.out.print("0x" + String.format("%x", sendData[i]) + " " );
      }

      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      clientSocket.receive(receivePacket);

      var response = new DnsResponse(receivePacket.getData());

      System.out.println("FROM SERVER: " );
      clientSocket.close();
    }catch(Exception e)
    {
      System.out.println(e.getMessage());
    }finally {
      if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }
  }
}
