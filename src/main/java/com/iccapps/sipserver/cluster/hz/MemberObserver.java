/* sip-cluster - a sip clustered application based on mobicents jain-sip.ha
 	and hazelcast backend. 

    Copyright (C) 2013-2014 Iñaki Cívico Campos.

    This file is part of sip-cluster.

    sip-cluster is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    sip-cluster is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with sip-cluster. If not, see <http://www.gnu.org/licenses/>.*/

package com.iccapps.sipserver.cluster.hz;

import org.apache.log4j.Logger;

import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class MemberObserver implements MembershipListener {

	private static Logger logger = Logger.getLogger(MemberObserver.class);
	
	public MemberObserver() {
		logger.info("Member observer created");
	}
	
	@Override
	public void memberAdded(MembershipEvent arg0) {
		logger.debug("Member added " + arg0.getMember().getUuid());
		
	}

	@Override
	public void memberRemoved(MembershipEvent ev) {
		logger.debug("Member removed " + ev.getMember().getUuid());
		ClusterImpl.getInstance().handleServerFailure(ev.getMember());
	}

}
