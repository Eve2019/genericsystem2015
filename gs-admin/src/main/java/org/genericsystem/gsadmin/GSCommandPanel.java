package org.genericsystem.gsadmin;

import org.genericsystem.ui.Element;
import org.genericsystem.ui.components.GSButton;
import org.genericsystem.ui.components.GSHBox;
import org.genericsystem.ui.components.GSLabel;

public class GSCommandPanel extends GSHBox {

	public GSCommandPanel(Element<?> parent) {
		super(parent);
	}

	@Override
	protected void initChildren() {
		this.setSpacing(10);
		new GSButton(this, "Flush", GenericWindow::flush);
		new GSButton(this, "Cancel", GenericWindow::cancel);
		new GSButton(this, "Mount", GenericWindow::mount);
		new GSButton(this, "Unmount", GenericWindow::unmount);
		new GSButton(this, "ShiftTs", GenericWindow::shiftTs);
		new GSLabel(this, GenericWindow::getCacheLevel);
	}
}
