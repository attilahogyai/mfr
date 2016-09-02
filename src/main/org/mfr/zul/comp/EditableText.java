package org.mfr.zul.comp;

import org.mfr.data.DictionaryDao;
import org.mfr.data.Site;
import org.mfr.util.HttpHelper;
import org.mfr.web.action.GlobalVariableResolver;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Textbox;
import org.mfr.data.Dictionary;


@VariableResolver(DelegatingVariableResolver.class)
public class EditableText extends HtmlMacroComponent {
	private static final long serialVersionUID = -3817877560414839285L;
	private String key;
	private String hclass;
	@WireVariable
	private DictionaryDao dictionaryDao;

	private Dictionary dict;
	
	
	
	@Wire
	private Textbox editbox;

	
	
	public EditableText() {
		super();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	protected void compose() {
		final Site site = GlobalVariableResolver.getSite();
		String language=GlobalVariableResolver.getLanguage();
		String text=null;
		if (site != null) {
			dict = dictionaryDao.findByKeySiteTemplate(key,
					site.getId(), site.getStyle());
			if(dict!=null){
				if(GlobalVariableResolver.LANG_HU.equals(language)){
					text=dict.getValue();
				}else if(GlobalVariableResolver.LANG_RO.equals(language)){
					text=dict.getValueRo();
				}else{
					text=dict.getValueEn();
				}
			}
		}else{
			text=Labels.getLabel(key);
		}
		setDynamicProperty("text", text);
		setDynamicProperty("key", key);
		setDynamicProperty("hclass", hclass);
		super.compose();
		
		if (editbox != null) {
			editbox.addEventListener("onChange", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					String language=GlobalVariableResolver.getLanguage();
					String vt = editbox.getValue();
					if(dict==null){
						dict=new Dictionary();
						dict.setKey(key);
						dict.setSite(site);
					}
					if(GlobalVariableResolver.LANG_HU.equals(language)){
						dict.setValue(vt);
					}else if(GlobalVariableResolver.LANG_RO.equals(language)){
						dict.setValueRo(vt);
					}else{
						dict.setValueEn(vt);
					}
					dictionaryDao.merge(dict);
				}

			});
		}
	}

	public String getHclass() {
		return hclass;
	}

	public void setHclass(String hclass) {
		this.hclass = hclass;
	}

}
