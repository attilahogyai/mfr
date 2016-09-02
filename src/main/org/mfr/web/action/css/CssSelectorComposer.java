package org.mfr.web.action.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Category;
import org.mfr.data.CategoryCss;
import org.mfr.data.CategoryCssDao;
import org.mfr.data.Css;
import org.mfr.data.CssDao;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Rows;

public class CssSelectorComposer extends SelectorComposer<Component> {
	public static String CATEGORY_TARGET="category";
	
	
	private static final Log log = LogFactory.getLog(CssSelectorComposer.class);
	@WireVariable
	private CssDao cssDao;
	@Wire
	private Rows styles;
	
	private Component parent;
	/**
	 * Contains the css group and css id 
	 */
	private Map<Integer,Integer> cssValues=new HashMap<Integer,Integer>();
	
	private List<Listbox> cssList=new ArrayList<Listbox>();
	
	private Object targetObject;
	/**
	 * Dis method can be overwrited by other css selector composer
	 */
	public void applyCssValues(){
		targetObject=(Category)Executions.getCurrent().getAttribute("category");
		CategoryCssDao categoryCssDao=(CategoryCssDao)SpringUtil.getBean("categoryCssDao");
		List<CategoryCss> categoryCssList=categoryCssDao.findByAlbum(((Category)targetObject).getId());
		for (CategoryCss categoryCss : categoryCssList) { // overwrite default selection
			cssValues.put(categoryCss.getCss().getGroup(), categoryCss.getCss().getId());
		}
	}
	public boolean isSelected(Integer group,Integer id){
		Integer v=cssValues.get(group);
		if(v!=null && v.equals(id)){
			return v.equals(id);
		}else{
			return false;
		}
	}

	protected void generateCssList(List<Css> cssList) {
		List<Css> byGroup=new ArrayList<Css>();
		if(cssList.size()>0){
			int group=cssList.get(0).getGroup();
			for (Css css : cssList) {
				if(css.getDef()!=null && css.getDef().intValue()!=0 && !cssValues.containsKey(css.getGroup())){
					cssValues.put(css.getGroup(), css.getId());
				}
				if(group!=css.getGroup().intValue()){ // generate group
					generateCssSelector(byGroup);
				    byGroup=new ArrayList<Css>();
				    group=css.getGroup();
				}
				byGroup.add(css);
			}
			generateCssSelector(byGroup);
		}
	}
	
	private void generateCssSelector(final List<Css> cssGroup) {
		if(cssGroup!=null && cssGroup.size()>0){
			Template t=parent.getTemplate("availableCss");
			Component[] comps = t.create(styles,
					null, new org.zkoss.xel.VariableResolver() {
						public Object resolveVariable(String variable) {
							if ("cssGroup".equals(variable)) {
								return cssGroup;
							}else if("groupId".equals(variable)) {
								return cssGroup.get(0).getGroup();
							}
							return null;
						}
					}, null);
			Listbox list=(Listbox)comps[0].getFirstChild();
			cssList.add(list);
		}
	}
	/**
	 * Dis method can be overwrited by other css selector composer
	 */
	@Listen("onClick= #applyStyle")
	public void applyStyle(){
		Category category=(Category)targetObject;
		CategoryCssDao categoryCssDao=(CategoryCssDao)SpringUtil.getBean("categoryCssDao");
		for (Listbox cssItem : cssList) {
			Integer groupId=(Integer)cssItem.getAttribute("group");
			CategoryCss categoryCss=categoryCssDao.findByGroupAndAlbum(groupId, category.getId());
			if(categoryCss==null){
				Css selectedStyle=cssDao.findById((Integer)cssItem.getSelectedItem().getValue());
				categoryCss=new CategoryCss();
				categoryCss.setCategory(category);
				categoryCss.setCss(selectedStyle);
				categoryCssDao.persist(categoryCss);
			}else if(!categoryCss.getCss().getId().equals(cssItem.getSelectedItem().getValue())){
				Css selectedStyle=cssDao.findById((Integer)cssItem.getSelectedItem().getValue());
				categoryCss.setCss(selectedStyle);
				categoryCssDao.merge(categoryCss);
			}
		}
		Messagebox.show(Labels.getLabel("style.saved",new Object[]{category.getId().toString()}),
				Labels.getLabel("information"), Messagebox.OK,
				Messagebox.INFORMATION);
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		this.parent=comp;
		super.doAfterCompose(comp);
		List<Css> cssList=cssDao.findByTargetObject(CATEGORY_TARGET);
		generateCssList(cssList);
	}

	public Map<Integer, Integer> getCssValues() {
		return cssValues;
	}

	public void setCssValues(Map<Integer, Integer> cssValues) {
		this.cssValues = cssValues;
	}
	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		applyCssValues();
		return super.doBeforeCompose(page, parent, compInfo);
	}
	
	

}
