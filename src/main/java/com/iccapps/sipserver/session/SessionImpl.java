package com.iccapps.sipserver.session;

import java.lang.reflect.InvocationTargetException;
import java.util.TimerTask;

import javax.sip.Transaction;

import org.apache.log4j.Logger;

import com.iccapps.sipserver.Endpoint;
import com.iccapps.sipserver.action.Action;
import com.iccapps.sipserver.action.Answer;
import com.iccapps.sipserver.action.Hangup;
import com.iccapps.sipserver.action.Reject;
import com.iccapps.sipserver.action.UpdateAck;
import com.iccapps.sipserver.api.Controller;
import com.iccapps.sipserver.api.Session;
import com.iccapps.sipserver.cluster.hz.ClusterImpl;

public class SessionImpl implements Session {
	
	private static Logger logger = Logger.getLogger(SessionImpl.class);

	private SessionState data;
	
	private Controller controller;
	private State state;
	
	private Transaction transaction = null;
	private TimerTask keepaliveTask;
	
	public SessionImpl() {
		data = new SessionState();
		state = new Initial(this);
		data.setState(state.getClass().getName());
	}
	
	public SessionImpl(SessionState s) throws IllegalArgumentException, SecurityException, 
				InstantiationException, IllegalAccessException, InvocationTargetException, 
				NoSuchMethodException, ClassNotFoundException {
		data = s;
		state = (State) Class.forName(data.getState()).getConstructor(SessionImpl.class).newInstance(this);
	}
	
	public void registerListener(Controller c) {
		controller = c;
	}
	
	public void update() {
		ClusterImpl.getInstance().update(this);
	}
	
	public void init() {
		
	}
	
	public void end() {
		if (keepaliveTask != null) keepaliveTask.cancel();
		
		transaction = null;
		controller = null;
		state = null;
		data = null;
	}
	
	public void dispatch(Action a) {
		logger.debug("Execute action " + a.toString() + " on channel " + SessionImpl.this.data.getDialogId());
		if (a instanceof Answer) {
			SessionImpl.this.data.setLocalSDP(((Answer) a).getSdp());
			SessionImpl.this.answer();
			
		} else if(a instanceof UpdateAck) {
			SessionImpl.this.updateAck();
			
		} else if(a instanceof Reject) {
			SessionImpl.this.hangup(((Reject) a).getCode());
			
		} else if(a instanceof Hangup) {
			SessionImpl.this.hangup();
		}
	}
	
	//*********** actions defined ***************//
	public void answer() {
		if (state != null) state.answer();
	}
	
	public void place(String destination) {
		
	}
	
	public void hangup() {
		if (state != null) state.hangup();
	}
	
	public void hangup(int reason) {
		if (state != null) state.reject(reason);
	}
	
	public void recovered() {
		if (state != null) {
			state.options();
		}
		// activate options
		keepaliveTask = new TimerTask() {
			@Override
			public void run() {
				state.options();
				
			}
		};
		Endpoint.getInstance().getTimer().schedule(keepaliveTask, 5000, 5000);
	}
	
	public void updateAck() {
		if (state != null) state.update();
	}
	
	//************ Events defined ******************//
	public void fireIncoming() {
		if (controller != null)
			controller.incoming(this);
	}
	
	public void fireConnected() {
		// activate options
		keepaliveTask = new TimerTask() {
			@Override
			public void run() {
				state.options();
				
			}
		};
		Endpoint.getInstance().getTimer().schedule(keepaliveTask, 5000, 5000);
		
		if (controller != null)
			controller.connected(this);
	}
	
	public void fireDisconnected() {
		if (keepaliveTask != null) keepaliveTask.cancel();
		
		if (controller != null)
			controller.disconnected(this);
	}
	
	public void fireProceeding() {
		if (controller != null)
			controller.progress(this);
	}
	
	public void fireTerminated() {
		if (keepaliveTask != null) keepaliveTask.cancel();
		
		if (controller != null)
			controller.terminated(this);
	}
	
	public void fireMessage(String message) {
		if (controller != null)
			controller.message(this, message);
	}
	
	public void fireDTMF() {
		if (controller != null)
			controller.dtmf(this);
	}
	
	public void fireUpdateMedia() {
		if (controller != null)
			controller.updateMedia(this);
	}
	
	// --------------------------------------------
	public String getDialogId() {
		return data.getDialogId();
	}
	
	public void setDialogId(String dialogId) {
		data.setDialogId(dialogId);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		this.data.setState(state.getClass().getName());
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public String getLocalSDP() {
		return data.getLocalSDP();
	}

	public void setLocalSDP(String localSDP) {
		data.setLocalSDP(localSDP);
	}

	public String getRemoteSDP() {
		return data.getRemoteSDP();
	}

	public void setRemoteSDP(String remoteSDP) {
		data.setRemoteSDP(remoteSDP);
	}

	public boolean isCanceled() {
		return data.isCanceled();
	}

	public void setCanceled(boolean canceled) {
		data.setCanceled(canceled);
	}

	public String getOriginationURI() {
		return data.getOriginURI();
	}

	public void setOriginURI(String originURI) {
		data.setOriginURI(originURI);
	}

	public String getDestinationURI() {
		return data.getDestinationURI();
	}

	public void setDestinationURI(String destinationURI) {
		data.setDestinationURI(destinationURI);
	}
	
	public String getReference() {
		return data.getReference();
	}

	public SessionState getData() {
		return data;
	}
}