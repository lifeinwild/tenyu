package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 非スレッドセーフ
 * @author exceptiontenyu@gmail.com
 *
 */
public class ServerList implements Storable {

	public static final int serverMax = 200;
	private List<NodeIdentifierUser> servers = new ArrayList<>();

	public boolean add(NodeIdentifierUser server) {
		if (contains(server))
			return false;
		return servers.add(server);
	}

	public boolean updateOrAdd(NodeIdentifierUser server) {
		delete(server);
		return add(server);
	}

	/**
	 * NodeIdentifierUserで重複判定をするので
	 * ノード番号が違えば同じユーザーIDでも多数登録できる。
	 *
	 * @param server
	 * @return
	 */
	public boolean contains(NodeIdentifierUser server) {
		for (int i = 0; i < servers.size(); i++) {
			NodeIdentifierUser e = servers.get(i);
			if (e.getUserId().equals(server)) {
				return true;
			}
		}
		return false;
	}

	public boolean delete(NodeIdentifierUser server) {
		boolean r = false;
		for (int i = 0; i < servers.size(); i++) {
			NodeIdentifierUser e = servers.get(i);
			if (e.equals(server)) {
				if (servers.remove(i) != null) {
					r = true;
				}
			}
		}
		return r;
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		//UserStore us = new UserStore(txn);
		for (NodeIdentifierUser server : servers) {
			if (!server.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	public List<NodeIdentifierUser> getServers() {
		return servers;
	}

	public List<NodeIdentifierUser> getServerIdentifiers() {
		List<NodeIdentifierUser> r = new ArrayList<>();
		for (NodeIdentifierUser e : servers) {
			r.add(e);
		}
		return r;
	}

	/*
	public List<Long> getServerUserIds() {
		List<Long> r = new ArrayList<>();
		for (NodeIdentifierUser e : servers) {
			Long id = e.getUserId();
			r.add(id);
		}
		return r;
	}
	*/

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (servers == null || servers.size() == 0) {
			r.add(Lang.STATICGAME_SERVERS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (servers.size() > serverMax) {
				r.add(Lang.STATICGAME_SERVERS, Lang.ERROR_TOO_MANY,
						servers.size() + " / " + serverMax);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (NodeIdentifierUser server : servers) {
				if (!server.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (NodeIdentifierUser server : servers) {
				if (!server.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		if (servers != null) {
			for (NodeIdentifierUser server : servers) {
				if (!server.validateAtDelete(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((servers == null) ? 0 : servers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerList other = (ServerList) obj;
		if (servers == null) {
			if (other.servers != null)
				return false;
		} else if (!servers.equals(other.servers))
			return false;
		return true;
	}

}