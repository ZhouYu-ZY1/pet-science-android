const fs = require('fs');

// 读取原始JSON文件
const rawData = fs.readFileSync('./wft9o-qlfdm.json', 'utf8');
const data = JSON.parse(rawData);

// 创建省市区的层级结构
const provinces = {};

// 第一遍循环，找出所有省份
data.forEach(item => {
  if (item.deep === "0") {
    provinces[item.id] = {
      name: item.ext_name || (item.name + "省"),
      city: []
    };
  }
});

// 第二遍循环，找出所有城市并关联到省份
const cities = {};
data.forEach(item => {
  if (item.deep === "1") {
    const cityObj = {
      name: item.ext_name || (item.name + "市"),
      area: []
    };
    cities[item.id] = cityObj;
    
    if (provinces[item.pid]) {
      provinces[item.pid].city.push(cityObj);
    }
  }
});

// 第三遍循环，找出所有区县并关联到城市
data.forEach(item => {
  if (item.deep === "2") {
    if (cities[item.pid]) {
      cities[item.pid].area.push(item.ext_name || (item.name + "区"));
    }
  }
});

// 转换为数组格式
const result = Object.values(provinces);

// 写入新的JSON文件
fs.writeFileSync('./province_new.json', JSON.stringify(result, null, 2));

console.log('转换完成！');