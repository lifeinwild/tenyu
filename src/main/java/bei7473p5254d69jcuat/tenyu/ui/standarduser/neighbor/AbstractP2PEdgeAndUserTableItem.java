package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import javafx.beans.property.*;

/**
 * P2PEdgeとUserの2つを合成したレコードをTableViewに表示する。
 * TODO やや変な設計。しかし多重継承はできないし
 * 委譲してメソッドを透過するような設計案しか思いつかない。
 * 現状の設計は、2つのモデルを合成したこのクラスがあり、
 * 子クラスがgetSrcを変えていてメインのモデルを切り替えている。
 * メインのモデルは操作対象となる情報を決定する。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <Src>
 */
public abstract class AbstractP2PEdgeAndUserTableItem<Src>
		implements TableRow<Src> {
	protected P2PEdge edge;
	private LongProperty userId = new SimpleLongProperty();
	private StringProperty name = new SimpleStringProperty();
	private StringProperty explanation = new SimpleStringProperty();

	private StringProperty addr = new SimpleStringProperty();
	private LongProperty latency = new SimpleLongProperty();
	private IntegerProperty impression = new SimpleIntegerProperty();
	private BooleanProperty dontRemove = new SimpleBooleanProperty();
	private StringProperty createDate = new SimpleStringProperty();

	public AbstractP2PEdgeAndUserTableItem(P2PEdge src) {
		this.edge = src;
		update();
	}

	abstract public Src getSrc();

	public void update() {
		setAddr(edge.getNode().getISAP2PPort().toString());
		setLatency(edge.getLatency());
		setImpression(edge.getImpression());
		setDontRemove(edge.isDontRemoveAutomatically());
		setCreateDate(edge.getCreateDateFormatForHuman());
	}

	public Long getUserId() {
		Long r = userId.get();
		return r;
	}

	public void setUserId(Long userId) {
		if (userId != null)
			this.userId.set(userId);
	}

	public P2PEdge getEdge() {
		return edge;
	}

	public String getName() {
		return name.get();
	}

	public StringProperty getExplanation() {
		return explanation;
	}

	public void setName(String name) {
		if (name != null)
			this.name.set(name);
	}

	public void setExplanation(String explanation) {
		if (explanation != null)
			this.explanation.set(explanation);
	}

	public String getAddr() {
		return addr.get();
	}

	public void setAddr(String addr) {
		if (addr != null)
			this.addr.set(addr);
	}

	public Long getLatency() {
		return latency.get();
	}

	//規約に従ったこれらPropertyを返すメソッドが無いとupdate()を呼んでもGUIが
	//更新されない。可変な情報だけ用意
	public LongProperty latencyProperty() {
		return latency;
	}

	public StringProperty addrProperty() {
		return addr;
	}

	public IntegerProperty impressionProperty() {
		return impression;
	}

	public BooleanProperty dontRemoveProperty() {
		return dontRemove;
	}

	public void setLatency(Long latency) {
		if (latency != null)
			this.latency.set(latency);
	}

	public Integer getImpression() {
		return impression.get();
	}

	public void setImpression(Integer impression) {
		if (impression != null)
			this.impression.set(impression);
	}

	public Boolean getDontRemove() {
		return dontRemove.get();
	}

	public void setDontRemove(Boolean dontRemove) {
		if (dontRemove != null) {
			this.dontRemove.set(dontRemove);
			edge.setDontRemoveAutomatically(dontRemove);
		}
	}

	public String getCreateDate() {
		return createDate.get();
	}

	public void setCreateDate(String createDate) {
		if (createDate != null)
			this.createDate.set(createDate);
	}

}
