package swingViews;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class _433SpringPanel_copilot extends JPanel {
	    private static final long serialVersionUID = 1L;
		private SpringLayout layout;
	    private Map<String, JPanel> slots = new LinkedHashMap<>();

	    public _433SpringPanel_copilot() {
	    	setOpaque(false);
	    	
	        layout = new SpringLayout();
	        setLayout(layout);

	        // 1) create the 11 “slot” panels
	        String[] names = {
	            "goalie",
	            "def1","def2","def3","def4",
	            "mid1","mid2","mid3",
	            "for1","for2","for3"
	        };
	        for (String n: names) {
	            JPanel slot = new JPanel(new FlowLayout());
	            slots.put(n, slot);
	            add(slot);
	        }
	        
	        addComponentListener(new ComponentAdapter() {
	            @Override
	            public void componentResized(ComponentEvent e) {
	              // update parent springs to actual size
	              SpringLayout.Constraints pCons = layout.getConstraints(_433SpringPanel_copilot.this);
	              pCons.setWidth (Spring.constant(getWidth()));
	              pCons.setHeight(Spring.constant(getHeight()));
	              // re‐compute children
	              applyConstraints();
	              revalidate();  // force SpringLayout to re‐run
	            }
	          });
	    }

	    private void applyConstraints() {
	    	SpringLayout.Constraints pCons = layout.getConstraints(this);
	    	// make the panel’s EAST = WEST + its real width
	    	pCons.setConstraint(SpringLayout.EAST, 
	    	    Spring.scale(Spring.constant(1), /*factor*/1f));
	    	pCons.setConstraint(SpringLayout.SOUTH,
	    	    Spring.scale(Spring.constant(1), /*factor*/1f));
	    	// now pCons.getWidth()==actual width, so your children see real numbers

	        // get the container’s springs
	        Spring widthS  = pCons.getWidth();
	        Spring heightS = pCons.getHeight();

	        // goalkeeper at 50% x, 90% y
	        SpringLayout.Constraints gCons = layout.getConstraints(slots.get("goalie"));
	        gCons.setX(Spring.scale(widthS, 0.5f));
	        gCons.setY(Spring.scale(heightS, 0.90f));

	        // defenders: 4 slots, convex arc
	        String[] defs = {"def1","def2","def3","def4"};
	        float[] defXs  = {0.10f, 0.30f, 0.70f, 0.90f};
	        float baseDY   = 0.60f;   // central Y
	        float arcAmp   = 0.05f;   // how deep the arc is
	        float midIndex = (defs.length - 1) / 2f;

	        for (int i = 0; i < defs.length; i++) {
	            JPanel slot    = slots.get(defs[i]);
	            float relX     = defXs[i];
	            // convex arc: outer defenders sit a bit lower
	            float relY     = baseDY + (Math.abs(i - midIndex) / midIndex) * arcAmp;

	            SpringLayout.Constraints c = layout.getConstraints(slot);
	            c.setX(Spring.scale(widthS, relX));
	            c.setY(Spring.scale(heightS, relY));
	        }

	        // midfielders: straight line
	        String[] mids = {"mid1","mid2","mid3"};
	        float[] midXs  = {0.25f, 0.50f, 0.75f};
	        float midY     = 0.40f;
	        for (int i = 0; i < mids.length; i++) {
	            SpringLayout.Constraints c = layout.getConstraints(slots.get(mids[i]));
	            c.setX(Spring.scale(widthS, midXs[i]));
	            c.setY(Spring.scale(heightS, midY));
	        }

	        // forwards: concave arc
	        String[] fwds = {"for1","for2","for3"};
	        float[] fwdXs = {0.25f, 0.50f, 0.75f};
	        float fwdBaseY = 0.15f;
	        float fwdAmp   = 0.03f;
	        float fwdMid   = (fwds.length - 1) / 2f;

	        for (int i = 0; i < fwds.length; i++) {
	            float relY = fwdBaseY - (Math.abs(i - fwdMid) / fwdMid) * fwdAmp;
	            SpringLayout.Constraints c = layout.getConstraints(slots.get(fwds[i]));
	            c.setX(Spring.scale(widthS, fwdXs[i]));
	            c.setY(Spring.scale(heightS, relY));
	        }
	        
	        for (JPanel slot : slots.values()) {
	        	  slot.setBorder(BorderFactory.createLineBorder(Color.RED));
	        	}
	    }

	    // your getters (unchanged)
	    public JPanel getGoalie() { return slots.get("goalie"); }
	    public JPanel getDef1()   { return slots.get("def1"); }
	    public JPanel getDef2()   { return slots.get("def2"); }
	    public JPanel getDef3()   { return slots.get("def3"); }
	    public JPanel getDef4()   { return slots.get("def4"); }
	    
	    public JPanel getMid1()   { return slots.get("mid1"); }
	    public JPanel getMid2()   { return slots.get("mid2"); }
	    public JPanel getMid3()   { return slots.get("mid3"); }
	    
	    public JPanel getForw1()   { return slots.get("for1"); }
	    public JPanel getForw2()   { return slots.get("for2"); }
	    public JPanel getForw3()   { return slots.get("for3"); }
	}

