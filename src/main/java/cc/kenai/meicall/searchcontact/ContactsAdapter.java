package cc.kenai.meicall.searchcontact;

import java.util.ArrayList;
import java.util.List;

import cc.kenai.meicall.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactsAdapter extends BaseAdapter{
	
	private List<Model> contactList;
	
	private Context context;
	
	private boolean isShowAll;
	
	public ContactsAdapter(Context context){
        this.contactList = new ArrayList<Model>();
        this.context = context;
	}
	
	public void refresh(List<Model> refreshList, boolean isShowAll){
		this.isShowAll = isShowAll;
		contactList.clear();
		contactList.addAll(refreshList);
		this.notifyDataSetChanged();
	}
	public void refresh_clear(){
		contactList.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return contactList.size();
	}

	@Override
	public Object getItem(int position) {
		return contactList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Model model = null;
		convertView = View.inflate(context, R.layout.lv_item, null);
		TextView tvname = (TextView) convertView.findViewById(R.id.tv_name);
		TextView tvtel = (TextView) convertView.findViewById(R.id.tv_telnum);
		TextView tvGroup = (TextView) convertView.findViewById(R.id.tv_group);
		model = contactList.get(position);
		if (model != null && tvname != null && tvtel != null&&tvGroup!=null) {
			tvname.setText(model.name);
			tvtel.setText(model.telnum);
			if (isShowAll) {
				model.group = "";
			}
			tvGroup.setText(model.group);
		}
		return convertView;
	}
	
}