#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>

@interface OCRImpl : CDVPlugin

- (void)startImageProcessing:(CDVInvokedUrlCommand*)command;

@end
