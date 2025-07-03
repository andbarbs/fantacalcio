package swingViews;

import javax.swing.JPanel;

public abstract class SchemeSpringPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	protected JPanel goalie;
	protected JPanel def1;
	protected JPanel def2;
	protected JPanel def3;
	protected JPanel def4;
	protected JPanel mid1;
	protected JPanel mid2;
	protected JPanel mid3;
	protected JPanel forw1;
	protected JPanel forw2;
	protected JPanel forw3;

	public JPanel getGoalie() {
		return goalie;
	}

	public JPanel getDef1() {
		return def1;
	}

	public JPanel getDef2() {
		return def2;
	}

	public JPanel getDef3() {
		return def3;
	}

	public JPanel getDef4() {
		return def4;
	}

	public JPanel getMid1() {
		return mid1;
	}

	public JPanel getMid2() {
		return mid2;
	}

	public JPanel getMid3() {
		return mid3;
	}

	public JPanel getForw1() {
		return forw1;
	}

	public JPanel getForw2() {
		return forw2;
	}

	public JPanel getForw3() {
		return forw3;
	}

}