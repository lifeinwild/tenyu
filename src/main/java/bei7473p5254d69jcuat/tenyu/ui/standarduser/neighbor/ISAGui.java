package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import java.net.*;

import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;

public class ISAGui extends ObjectGui<InetSocketAddress> {

	public ISAGui(String name, String id) {
		super(name, id);
	}

	@Override
	public void set(InetSocketAddress o) {
		addrInput.setText(o.getAddress().toString());
		portInput.setText(o.getPort() + "");
	}

	private String addr;
	private byte[] addrB;

	private TextField addrInput;
	//アドレス
	private Label addrLabel;

	private int port;
	private TextField portInput;
	//ポート
	private Label portLabel;

	@Override
	public GridPane buildCreate() {
		super.buildCreate();
		//アドレス
		setAddrLabel(
				new Label(Lang.NECESSARY.toString() + Lang.IPADDR.toString()));
		getAddrLabel().setId(idPrefix + "AddrLabel");
		getAddrLabel().setFocusTraversable(true);
		grid.add(getAddrLabel(), 0, elapsed);
		setAddrInput(new TextField());
		getAddrInput().setId(idPrefix + "AddrInput");
		grid.add(getAddrInput(), 1, elapsed);

		elapsed += 1;

		//ポート
		setPortLabel(
				new Label(Lang.NECESSARY.toString() + Lang.PORT.toString()));
		getPortLabel().setId(idPrefix + "PortLabel");
		getPortLabel().setFocusTraversable(true);
		grid.add(getPortLabel(), 0, elapsed);
		setPortInput(new TextField());
		getPortInput().setId(idPrefix + "PortInput");
		grid.add(getPortInput(), 1, elapsed);

		elapsed += 1;

		//登録
		SubmitButtonGui registerButtonGui = buildSubmitButton(gui -> {
			if (gui == null) {
				//guiがnullだとアラートしか通知の方法が無い
				Glb.getGui().alert(AlertType.ERROR, name,
						Lang.EXCEPTION.toString());
				return false;
			}
			gui.message("", Color.BLACK);

			try {
				String portSrc = getPortInput().getText().trim();
				setPort(Integer.valueOf(portSrc));
			} catch (Exception e) {
				gui.message(Lang.PORT_INVALID.toString());
				return false;
			}

			try {
				InetSocketAddress isaTmp = null;
				setAddr(getAddrInput().getText().trim());
				if (getAddr() == null || getAddr().length() == 0)
					throw new Exception();
				isaTmp = new InetSocketAddress(getAddr(), getPort());
				if (isaTmp.getAddress() == null)
					throw new Exception();
				setAddrB(isaTmp.getAddress().getAddress());
				if (getAddrB() == null || getAddrB().length == 0)
					return false;
			} catch (Exception e) {
				gui.message(Lang.IPADDR_INVALID.toString());
				return false;
			}

			return true;
		}, gui -> Recognition.send(getAddrB(), getPort()), gui -> {
			clear();

			//作成されたエッジ
			P2PEdge n = Glb.getSubje().getNeighborList().getNeighbor(getAddrB(),
					getPort());

			//手動追加したので自動削除不可フラグを立てる
			if (n != null)
				n.setDontRemoveAutomatically(true);
		}, null);
		elapsed += 2;
		return grid;
	}

	public void clear() {
		if (portInput != null)
			portInput.clear();
		if (addrInput != null)
			addrInput.clear();
	}

	public String getAddr() {
		return addr;
	}

	public byte[] getAddrB() {
		return addrB;
	}

	public TextField getAddrInput() {
		return addrInput;
	}

	public Label getAddrLabel() {
		return addrLabel;
	}

	public int getPort() {
		return port;
	}

	public TextField getPortInput() {
		return portInput;
	}

	public Label getPortLabel() {
		return portLabel;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public void setAddrB(byte[] addrB) {
		this.addrB = addrB;
	}

	public void setAddrInput(TextField addrInput) {
		this.addrInput = addrInput;
	}

	public void setAddrLabel(Label addrLabel) {
		this.addrLabel = addrLabel;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPortInput(TextField portInput) {
		this.portInput = portInput;
	}

	public void setPortLabel(Label portLabel) {
		this.portLabel = portLabel;
	}

}