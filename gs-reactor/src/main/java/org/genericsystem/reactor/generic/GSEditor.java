package org.genericsystem.reactor.generic;

import org.genericsystem.api.core.ApiStatics;
import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.generic.GSLinks.LinkAdder;
import org.genericsystem.reactor.generic.GSLinks.LinkEditor;
import org.genericsystem.reactor.generic.GSLinks.LinkEditorWithRemoval;
import org.genericsystem.reactor.generic.GSLinks.LinkTitleDisplayer;
import org.genericsystem.reactor.html.HtmlH1;
import org.genericsystem.reactor.model.GenericModel;
import org.genericsystem.reactor.model.ObservableListExtractor;
import org.genericsystem.reactor.model.StringExtractor;

/**
 * @author Nicolas Feybesse
 *
 * @param <M>
 */
public class GSEditor extends GSComposite {

	public GSEditor(Tag<?> parent) {
		this(parent, FlexDirection.COLUMN);
	}

	public GSEditor(Tag<?> parent, FlexDirection flexDirection) {
		super(parent, flexDirection);
		addStyle("flex", "1");
	}

	@Override
	protected void header() {
		new GSSection(this, GSEditor.this.getReverseDirection()) {
			{
				addStyle("flex", "0.3");
				addStyle("background-color", "#ffa500");
				addStyle("margin-right", "1px");
				addStyle("margin-bottom", "1px");
				addStyle("color", "red");
				addStyle("justify-content", "center");
				addStyle("align-items", "center");
				new HtmlH1<GenericModel>(this) {
					{
						bindText(GenericModel::getString);
					}
				};
			}
		};
	}

	@Override
	protected void sections() {

		new GSComposite(this, GSEditor.this.getReverseDirection()) {
			{
				addStyle("flex", "1");
			}

			@Override
			protected void header() {
				new GSSection(this, GSEditor.this.getDirection()) {
					{
						addStyle("flex", "0.3");
						new LinkTitleDisplayer(this, gs -> ObservableListExtractor.COMPONENTS.apply(gs).filtered(g -> !g.equals(gs[1].getMeta())), GSEditor.this.getDirection()) {
							{
								addStyle("flex", "1");
								addStyle("overflow", "hidden");
								select(StringExtractor.SIMPLE_CLASS_EXTRACTOR, gs -> gs[0].getMeta());
							}
						};
						new LinkTitleDisplayer(this, gs -> ObservableListExtractor.COMPONENTS.apply(gs).filtered(g -> !g.equals(gs[1].getMeta())), GSEditor.this.getDirection()) {
							{
								addStyle("flex", "1");
								addStyle("overflow", "hidden");
								forEach(StringExtractor.SIMPLE_CLASS_EXTRACTOR, ObservableListExtractor.ATTRIBUTES_OF_INSTANCES);
							}
						};
					}
				};
			}

			@Override
			protected void sections() {
				new GSSection(this, GSEditor.this.getDirection()) {
					{
						addStyle("flex", "1");
						new GSSection(this, GSEditor.this.getReverseDirection()) {
							{
								addStyle("flex", "1");
								addStyle("overflow", "hidden");
								new LinkEditor(this, GSEditor.this.getDirection()) {
									{
										addStyle("flex", "1");
										select(StringExtractor.SIMPLE_CLASS_EXTRACTOR, gs -> gs[0]);
									}
								};
							}
						};
						new GSSection(this, GSEditor.this.getReverseDirection()) {
							{
								forEach(StringExtractor.SIMPLE_CLASS_EXTRACTOR, ObservableListExtractor.ATTRIBUTES_OF_INSTANCES);
								addStyle("flex", "1");
								addStyle("overflow", "hidden");
								new LinkEditorWithRemoval(this, GSEditor.this.getDirection()) {
									{
										addStyle("flex", "1");
										forEach(StringExtractor.SIMPLE_CLASS_EXTRACTOR, ObservableListExtractor.HOLDERS);
									}
								};
								new LinkAdder(this, GSEditor.this.getDirection()) {
									{
										addStyle("flex", "1");
										select(StringExtractor.SIMPLE_CLASS_EXTRACTOR, gs -> ObservableListExtractor.HOLDERS.apply(gs).isEmpty() || (gs[0].getComponents().size() < 2 && !gs[0].isPropertyConstraintEnabled())
												|| (gs[0].getComponents().size() >= 2 && !gs[0].isSingularConstraintEnabled(ApiStatics.BASE_POSITION)) ? gs[0] : null);
									}
								};
							}
						};
					}
				};
			};
		};
	}
}