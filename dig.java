// Copyright (c) 1999 Brian Wellington (bwelling@anomaly.munge.com)
// Portions Copyright (c) 1999 Network Associates, Inc.

import java.io.*;
import java.util.*;

public class dig {

static dnsName name = null;
static short type = dns.A, _class = dns.IN;

static void
usage() {
	System.out.println("Usage: dig [@server] name [<type>] [<class>]");
	System.exit(0);
}

static void
doQuery(dnsMessage query, dnsResolver res) throws IOException {
	dnsMessage response;

	System.out.println("; java dig 0.0");

	response = res.send(query);
	if (response == null)
		return;

	System.out.print(";; ->>HEADER<<- ");
	System.out.print("opcode: ");
	System.out.print(dns.opcodeString(response.getHeader().getOpcode()));
	System.out.print(", status: ");
	System.out.print(dns.rcodeString(response.getHeader().getRcode()));
	System.out.println(", id: " + response.getHeader().getID());

	
	System.out.print(";; flags: " + response.getHeader().printFlags());
	System.out.print("; ");
	for (int i = 0; i < 4; i++) {
		System.out.print(dns.sectionString(i));
		System.out.print(": ");
		System.out.print(response.getHeader().getCount(i));
		System.out.print(" ");
	}
	System.out.println();

	if (response.getHeader().getCount(dns.QUESTION) > 0) {
		int i = dns.QUESTION;
		System.out.println(";; " + dns.longSectionString(i) + ":");
		Enumeration e = response.getSection(i).elements();
		while (e.hasMoreElements()) {
			dnsRecord rec = (dnsRecord) e.nextElement();
			System.out.print(";;\t");
			System.out.print(rec.name);
			System.out.print(", type = ");
			System.out.print(dns.typeString(rec.type));
			System.out.print(", class = ");
			System.out.println(dns.classString(rec.dclass));
		}
		System.out.println();
	}

	for (int i = 1; i < 4; i++) {
		if (response.getHeader().getCount(i) == 0)
			continue;

		System.out.println(";; " + dns.longSectionString(i) + ":");
		Enumeration e = response.getSection(i).elements();
		while (e.hasMoreElements()) {
			System.out.println(e.nextElement());
		}
		System.out.println();
	}
	System.out.println(";; done (" + response.numBytes() + " bytes)");
}

static void
doAXFR(dnsMessage query, dnsResolver res) throws IOException {
	dnsMessage response;

	System.out.println("; java dig 0.0 <> " + name + " axfr");

	response = res.sendAXFR(query);
	if (response == null)
		return;

	Enumeration e = response.getSection(dns.ANSWER).elements();
	while (e.hasMoreElements())
		System.out.println(e.nextElement());

	System.out.print(";; done (");
	System.out.print(response.getSection(dns.ANSWER).size());
	System.out.print(" records, ");
	System.out.print(response.getSection(dns.ADDITIONAL).size());
	System.out.println(" additional)");
}

public static void
main(String argv[]) throws IOException {
	String server;
	int arg;
	dnsMessage query = new dnsMessage();
	dnsRecord rec;
	dnsResolver res = null;

	query.getHeader().setFlag(dns.RD);
	query.getHeader().setOpcode(dns.QUERY);

	if (argv.length < 1) {
		usage();
	}

	try {
		arg = 0;
		if (argv[arg].startsWith("@")) {
			server = argv[arg++].substring(1);
			res = new dnsResolver(server);
		}
		else
			res = new dnsResolver("localhost");

		name = new dnsName(argv[arg++]);

		type = dns.typeValue(argv[arg]);
		if (type < 0)
			type = dns.A;
		else
			arg++;

		_class = dns.classValue(argv[arg]);
		if (_class < 0)
			_class = dns.IN;
		else
			arg++;

		while (argv[arg].startsWith("-") && argv[arg].length() > 1) {
			switch (argv[arg].charAt(1)) {
			    case 'p':
				String portStr;
				int port;
				if (argv[arg].length() > 2)
					portStr = argv[arg].substring(2);
				else
					portStr = argv[++arg];
				port = Integer.parseInt(portStr);
				if (port < 0 || port > 65536) {
					System.out.println("Invalid port");
					return;
				}
				res.setPort(port);
				break;

			    case 'k':
				String key;
				if (argv[arg].length() > 2)
					key = argv[arg].substring(2);
				else
					key = argv[++arg];
				int index = key.indexOf('/');
				if (index < 0)
					res.setTSIGKey(key);
				else
					res.setTSIGKey(key.substring(0, index),
						       key.substring(index+1));
				break;

			    default:
				System.out.print("Invalid option");
				System.out.println(argv[arg]);
			}
			arg++;
		}

	}
	catch (ArrayIndexOutOfBoundsException e) {
		if (name == null)
			usage();
	}

	rec = dnsRecord.newRecord(name, type, _class);
	query.addRecord(dns.QUESTION, rec);

	if (type == dns.AXFR)
		doAXFR(query, res);
	else
		doQuery(query, res);
}

}
