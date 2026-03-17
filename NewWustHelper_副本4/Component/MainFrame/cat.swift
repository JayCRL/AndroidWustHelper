//
//  Cat.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/11/23.
//

import SwiftUI
import Foundation

// 内容卡片的数据模型
struct FeedItem: Identifiable {
    let id = UUID()
    let type: String // "video" 或 "post"
    let title: String
    let author: String
    let likes: Int
    let imageUrl: String? // 可选的图片 URL 或名称
    let tag: String? // 标签，例如“热点”
}

// 示例数据
let mockItems: [FeedItem] = [
    FeedItem(type: "post", title: "武汉近期吃到最牛逼好吃的一碗。。。", author: "橘子好甜", likes: 56, imageUrl: "image_food_large", tag: nil),
    FeedItem(type: "video", title: "一行代码让全球崩了6小时", author: "敖丙", likes: 4089, imageUrl: "image_code_bug", tag: nil),
]

// 辅助：用于创建像图片中那种上下两张或一大张的内容卡片
struct ContentCardView: View {
    let item: FeedItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            // 假设使用异步加载或本地图片
            Image(item.imageUrl ?? "placeholder_image")
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 150)
                .clipped()
            
            Text(item.title)
                .font(.system(size: 15))
                .lineLimit(2)
                .foregroundColor(.black)
            
            HStack {
                if let tag = item.tag {
                    // 左下角“热点”标签
                    Text(tag)
                        .font(.system(size: 12))
                        .foregroundColor(.red)
                        .padding(.vertical, 2)
                        .padding(.horizontal, 5)
                        .background(
                            RoundedRectangle(cornerRadius: 3)
                                .stroke(Color.red, lineWidth: 1)
                        )
                }
                
                Text(item.author)
                    .font(.caption)
                    .foregroundColor(.gray)
                
                Spacer()
                
                // 点赞图标和数量
                HStack(spacing: 3) {
                    Image(systemName: "heart.fill")
                        .foregroundColor(.gray)
                        .font(.system(size: 12))
                    Text("\(item.likes)")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(8)
        .background(Color.white)
        .cornerRadius(10)
        .shadow(radius: 2)
    }
}
struct cat: View {
    var body: some View {
        Text(/*@START_MENU_TOKEN@*/"Hello, World!"/*@END_MENU_TOKEN@*/)
    }
}

#Preview {
    cat()
}
