package swingViews;

import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SpringSchemePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel goalie;
	private JPanel def1;
	private JPanel def2;
	private JPanel def3;
	private JPanel def4;
	private JPanel mid1;
	private JPanel mid2;
	private JPanel mid3;
	private JPanel forw1;
	private JPanel forw2;
	private JPanel forw3;

	public SpringSchemePanel() {
		setOpaque(false);
		setLayout(new SpringLayout());
		
		goalie = new JPanel();
		def1 = new JPanel(); 
		def2  = new JPanel(); 
		def3 = new JPanel(); 
		def4 = new JPanel();
		mid1 = new JPanel();
		mid2 = new JPanel();
		mid3 = new JPanel();
		forw1 = new JPanel();
		forw2 = new JPanel();
		forw3 = new JPanel();

		Stream.of(goalie,
				def1, def2, def3, def4,
				mid1, mid2, mid3,
				forw1, forw2, forw3
				).forEach(this::add);
	}
	
	@Override
	public SpringLayout getLayout() {
		return (SpringLayout) super.getLayout();
	}

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