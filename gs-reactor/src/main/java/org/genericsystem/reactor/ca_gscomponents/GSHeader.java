package org.genericsystem.reactor.ca_gscomponents;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.ca_gscomponents.GSSubcellDisplayer.TagConstructor;

public class GSHeader extends GSDiv {

	public GSHeader(Tag parent, String string, TagConstructor tag1, String string1, TagConstructor tag2, String string2) {
		super(parent, FlexDirection.ROW);
		addStyle("justify-content", "space-around");
		addStyle("padding", "10px");
		if (tag1 != null) {
			Tag leftTag = tag1.build(this);
			leftTag.setText(string1);
			leftTag.addStyle("flex", "1");
		} else {
			Tag leftTag = new GSDiv(this, FlexDirection.COLUMN);
			leftTag.addStyle("flex", "1");
		}
		new GenericH2Section(this, string) {
			{
				addStyle("flex", "3");
				addStyle("align-items", "center");
				addStyle("color", "White");
				addStyle("text-shadow", "1px 1px 2px black, 0 0 25px blue, 0 0 5px darkblue");
			}
		};
		if (tag2 != null) {
			Tag rightTag = tag2.build(this);
			rightTag.setText(string2);
			rightTag.addStyle("flex", "1");
		} else {
			Tag rightTag = new GSDiv(this, FlexDirection.COLUMN);
			rightTag.addStyle("flex", "1");
		}
	};

}