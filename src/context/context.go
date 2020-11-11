package context

import "strings"
import "fmt"
import "os"
import "log"
import "io"

import "gopkg.in/ini.v1"

import "../submit"

type FeedsContext struct {
	Config *ini.File
	DevoSender *submit.DevoSender
	Logger *log.Logger
	LoggerFile *os.File
}

func (context *FeedsContext) New(config *ini.File, devosender *submit.DevoSender) {
	context.Config = config
	context.DevoSender = devosender

	log_file := config.Section("DEFAULT").Key("log_file").String()

	var err error
	
	if strings.Compare(log_file, "") == 0 {
		fmt.Println("No log file set, using stdout.")
		return
	} else {
		context.LoggerFile, err = os.OpenFile(log_file, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0644)
		if err != nil {
			fmt.Println("Error opening log file. Using stdout:", err)
		}
		
		logwriter := io.MultiWriter(context.LoggerFile)
		context.Logger = log.New(logwriter, "", log.Ldate|log.Ltime)
		
		return
	}
}

func (context *FeedsContext)Log(feed_name string, v ...interface{}) {
	feedname := "[" + feed_name + "] "
	printout := append([]interface{}{feedname}, v...)	
	context.Logger.Println(printout...)
}


func (context *FeedsContext) Close() {
	context.LoggerFile.Close()
}
