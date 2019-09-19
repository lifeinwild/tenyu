package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

/**
 * インターフェース用。DBに保存される形式ではない。
 * @author exceptiontenyu@gmail.com
 *
 */
public class RefToMaterial {
	private Material material;
	private String ref;

	public RefToMaterial(String ref, Material material) {
		this.ref = ref;
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public String getRef() {
		return ref;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
}