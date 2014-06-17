package org.kramerlab.bmad.visualization;

import java.awt.*;
import javax.swing.*;
import static java.lang.Math.*;

@SuppressWarnings("serial")
public class SingleImageComponent extends JComponent {

	private final Image image;
	
	public SingleImageComponent(Image image) {
		this.image = image;
	}
	
	@Override
	public void paint(Graphics g) {
		int w = image.getWidth(this);
		int h = image.getHeight(this);
		double widthProportion = getWidth() / (double)w;
		double heightProportion = getHeight() / (double)h;
		double scaleFactor = min(widthProportion, heightProportion);
		double scaledW = scaleFactor * w;
		double scaledH = scaleFactor * h;
		g.drawImage(image, (int)((getWidth() - scaledW)/2), (int)((getHeight() - scaledH)/2), (int)scaledW, (int)scaledH, null);
	}
}
