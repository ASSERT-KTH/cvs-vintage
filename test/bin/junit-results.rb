#!/usr/bin/ruby

# Author:  Vadim Nasardinov (vadimn@redhat.com)
# Since:   2004-12-09
# Version: $Id: junit-results.rb,v 1.3 2005/02/04 16:27:56 el-vadimo Exp $

# A quick hack for generating a summary of JUnit results for CAROL

require 'find'

HEADER = <<-"EOD"
<html>
<head>
  <title>Test Results</title>
</head>
<body>
<table border="1" cellpadding="2" cellspacing="0">
<tr>
  <th>Suite</th>
  <th>Tests</th>
  <th>Failures</th>
  <th>Errors</th>
</tr>
EOD

FOOTER = <<-"EOD"
</table>
</body>
</html>
EOD

RESULT_DIR = "output/dist/test/results"

def extract_results(path)
    overview = File.join(File.dirname(path), "overview-summary.html")
    n_tests, n_failed, n_errors = 0, 0, 0

    state = nil
    File.open(overview) do |ff|
        ff.each do |line|
            line.strip!
            if state == "summary"
                if line == "<th>Tests</th><th>Failures</th><th>Errors</th><th>Success rate</th><th>Time</th>"
                    state = "ready"
                end
            elsif state == "ready"
                if line[0..3] == "<td>"
                    cells = line[4..-6].split("</td><td>")
                    return cells[0], cells[1], cells[2]
                end
            else
                if line == "<h2>Summary</h2>"
                    state = "summary"
                end
            end
        end
    end
    raise "Unexpected end of file: #{path}"
end

def cmp(report1, report2)
    testname1, process1 = report1[-2..-1]
    testname2, process2 = report2[-2..-1]
    is_multi1 = (testname1[0..4] == "multi")
    is_multi2 = (testname2[0..4] == "multi")
    if (is_multi1 and is_multi2) or (!is_multi1 and !is_multi2)
        if testname1 == testname2
            return process1 <=> process2
        else
            return testname1 <=> testname2
        end
    else
        if is_multi1
            return 1
        else
            return -1
        end
    end
end

def collect_reports()
    reports = []
    Find.find(RESULT_DIR) do |path|
        if FileTest.file?(path) and File.basename(path) == "index.html"
            relpath = path[RESULT_DIR.length + 1.. -1]
            if relpath != "index.html"
                testname = relpath.split("/")[0]
                idx = relpath.index("/parallel")
                if idx == nil
                    process = ""
                else
                    idx = idx + "/parallel".length
                    process = "[process ##{relpath[idx..idx]}]"
                end
                reports.push([path, relpath, testname, process])
            end
        end
    end
    return reports
end

RESULT_PAGE = "#{RESULT_DIR}/index.html"

File.open(RESULT_PAGE, "w") do |ff|
    ff.write(HEADER)

    reports = collect_reports()
    reports = reports.sort{|x, y| cmp(x, y)}

    reports.each do |report|
        path, relpath, testname, process = report
        n_tests, n_failed, n_errors = extract_results(path)

        if testname == "multi_suniiop_jeremie" or \
            (testname == "multi_suniiop_jeremie.nons" and process == "[process #1]")

            style = ' style="background-color: PeachPuff;"'
        else
            style = ''
        end

        ff.write("<tr#{style}>\n<td><a href=\"#{relpath}\">#{testname}</a> #{process}</td>\n")
        ff.write("<td>#{n_tests}</td>")
        ff.write("<td>#{n_failed}</td>")
        ff.write("<td>#{n_errors}</td>\n</tr>\n")
    end

    ff.write(FOOTER)
end

puts "Wrote\n#{RESULT_PAGE}"
