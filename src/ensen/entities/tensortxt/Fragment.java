package ensen.entities.tensortxt;

import java.util.ArrayList;

import ensen.entities.EnsenDBpediaResource;

public class Fragment {
	public String text;
	public int id;
	public int start;
	public int end;
	public ArrayList<EnsenDBpediaResource> resources;

	public Fragment(int i, String t, int s, int e) {
		id = i;
		text = t;
		start = s;
		end = e;
		resources = new ArrayList<EnsenDBpediaResource>();
	}

	public boolean containResource(String uri) {
		for (int i = 0; i < resources.size(); i++) {
			if (resources.get(i).getFullUri().contains(uri))
				return true;
		}
		return false;
	}
}
