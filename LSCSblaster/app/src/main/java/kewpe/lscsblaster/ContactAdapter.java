package kewpe.lscsblaster;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    private ArrayList<Contact> contacts;
    ContactAdapter.ContactHolder holder;

    public ContactAdapter(ArrayList<Contact> contacts){
        this.contacts = contacts;
    }

    @Override
    public ContactAdapter.ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list, null);

        holder = new ContactHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ContactHolder holder, int position) {
        final int pos = position;

        Contact contact = contacts.get(position);
        holder.cbx_contact.setText(contact.getName());
        holder.cbx_contact.setTag(contacts.get(position));

        holder.cbx_contact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contact contact = (Contact) cb.getTag();

                contact.setSelected(cb.isChecked());
                contacts.get(pos).setSelected(cb.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
            return this.contacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        CheckBox cbx_contact;

        public ContactHolder(View itemView) {
            super(itemView);
            cbx_contact = (CheckBox) itemView.findViewById(R.id.cbx_contact);
        }
    }
}
