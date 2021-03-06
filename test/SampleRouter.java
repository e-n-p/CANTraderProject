import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import javax.net.ServerSocketFactory;

import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;

public class SampleRouter extends Thread implements Router{
	private static final Random RANDOM_NUM_GENERATOR=new Random();
	private static final Instrument[] INSTRUMENTS={new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private Socket objectManagerConnection;
	private int port;
	ObjectInputStream objectInputStream;
	ObjectOutputStream objectOutputStream;
	public SampleRouter(String name,int port){
		this.setName(name);
		this.port=port;
	}

	public void run(){
		//OM will connect to us
		try {
			objectManagerConnection =ServerSocketFactory.getDefault().createServerSocket(port).accept();
			while(true){
				if(objectManagerConnection.getInputStream().available()>0){
					objectInputStream=new ObjectInputStream(objectManagerConnection.getInputStream());
					orderRequest methodName=(orderRequest)objectInputStream.readObject();
					System.out.println("Order Router received method call for:"+methodName);
					switch(methodName){
						case routeOrder:
							routeOrder(objectInputStream.readInt(),objectInputStream.readInt(),objectInputStream.readInt(),(Instrument)objectInputStream.readObject());break;
						case priceAtSize:
							priceAtSize(objectInputStream.readInt(),objectInputStream.readInt(),(Instrument)objectInputStream.readObject(),objectInputStream.readInt());break;
					}
				}else{
					Thread.sleep(100);
				}
			}
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void routeOrder(int id,int sliceId,int size,Instrument i) throws IOException, InterruptedException{ //MockI.show(""+order);
		int fillSize=RANDOM_NUM_GENERATOR.nextInt(size);
		//TODO have this similar to the market price of the instrument
		double fillPrice=199*RANDOM_NUM_GENERATOR.nextDouble();
		Thread.sleep(42);
		objectOutputStream=new ObjectOutputStream(objectManagerConnection.getOutputStream());
		objectOutputStream.writeObject("newFill");
		objectOutputStream.writeInt(id);
		objectOutputStream.writeInt(sliceId);
		objectOutputStream.writeInt(fillSize);
		objectOutputStream.writeDouble(fillPrice);
		objectOutputStream.flush();
	}

	@Override
	public void sendCancel(int id,int sliceId,int size,Instrument i){ //MockI.show(""+order);
	}

	@Override
	public void priceAtSize(int id, int sliceId,Instrument i, int size) throws IOException{
		objectOutputStream=new ObjectOutputStream(objectManagerConnection.getOutputStream());
		objectOutputStream.writeObject("bestPrice");
		objectOutputStream.writeInt(id);
		objectOutputStream.writeInt(sliceId);
		objectOutputStream.writeDouble(199*RANDOM_NUM_GENERATOR.nextDouble());
		objectOutputStream.flush();
	}
}
